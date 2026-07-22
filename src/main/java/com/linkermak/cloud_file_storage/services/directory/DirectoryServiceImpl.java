package com.linkermak.cloud_file_storage.services.directory;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    @Override
    public List<StorageResource> getResourcesByPath(String pathDirectory) {
        String normalizePath = pathPreparer.prepareDirectoryPath(pathDirectory);

        Long userId = userProvider.currentUserId();

        // сделать обший validateResourceExists, который внутри сам будет определять папка это или файл и проверять
        if (!storageRepository.existsDirectory(userId, normalizePath)) {
            throw new ResourceNotFoundException("Directory not found by path:" + normalizePath);
        }

        List<StorageObjectInfo> objectInfoResources =
                storageRepository.findResourcesByPrefix(userId, normalizePath);

        return generateStorageResources(objectInfoResources);
    }

    private List<StorageResource> generateStorageResources(List<StorageObjectInfo> objectInfoResources) {
        List<StorageResource> storageResources = new ArrayList<>();
        for (StorageObjectInfo resourceInfo : objectInfoResources) {
            String key = resourceInfo.path();

            boolean isDirectory = key.endsWith("/");

            storageResources.add(
                    new StorageResource(
                            StoragePathExtractor.extractParentPath(key).orElse(""),
                            StoragePathExtractor.extractLastPath(key),
                            isDirectory ? null : resourceInfo.size(),
                            isDirectory ? StorageResourceType.DIRECTORY : StorageResourceType.FILE
                    )
            );
        }

        return storageResources;
    }


    @Override
    @Transactional
    public StorageResource createDirectory(String pathDirectory) {
        String normalizePath = pathPreparer.prepareDirectoryPath(pathDirectory);

        Long userId = userProvider.currentUserId();
        Optional<String> parentPath = StoragePathExtractor.extractParentPath(normalizePath);

        if (parentPath.isPresent()
                && !storageRepository.existsDirectory(userId, parentPath.get())) {
            throw new ResourceNotFoundException("Parent directory not found by path:" + normalizePath);
        }

        if (storageRepository.existsDirectory(userId, normalizePath)) {
            throw new ResourceAlreadyExistsException("Directory already exists by path:" + normalizePath);
        }

        storageRepository.createDirectory(userId, normalizePath);
        return new StorageResource(
                parentPath.orElse(""),
                StoragePathExtractor.extractLastPath(normalizePath),
                null,
                StorageResourceType.DIRECTORY
        );
    }

    @Override
    public void validateDirectoryExists(String pathDirectory) {
        String preparedPath = pathPreparer.prepareDirectoryPath(pathDirectory);
        if (!storageRepository.existsDirectory(userProvider.currentUserId(), preparedPath)) {
            throw new ResourceNotFoundException("Directory not found");
        }
    }

}
