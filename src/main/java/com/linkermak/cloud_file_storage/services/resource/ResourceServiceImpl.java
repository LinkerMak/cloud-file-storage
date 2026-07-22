package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final DirectoryService directoryService;

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    @Override
    public StorageResource getResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);

        boolean isDirectory = trimmedPath.endsWith("/");

        // TODO: возможно лучше сделать через Optional и выброс ошибки либо проверять наличие ресурса заранее
        StorageObjectInfo objectInfo = storageRepository.getResourceInfoByPath(
                userProvider.currentUserId(),
                isDirectory ?
                        pathPreparer.prepareDirectoryPath(trimmedPath) :
                        pathPreparer.prepareFilePath(trimmedPath)
        );

        return new StorageResource(
                StoragePathExtractor.extractParentPath(objectInfo.path()).orElse(""),
                StoragePathExtractor.extractLastPath(objectInfo.path()),
                isDirectory ? null : objectInfo.size(),
                isDirectory ? StorageResourceType.DIRECTORY : StorageResourceType.FILE
        );
    }

    @Override
    public void deleteResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);
        boolean isDirectory = trimmedPath.endsWith("/");
        isDirectory ? deleteDirectory(path) : deleteFile(path);
    }

    private void deleteFile(String path) {
        Long userId = userProvider.currentUserId();
        String preparedPath = pathPreparer.prepareFilePath(path);

        if(!storageRepository.existsFile(userId, preparedPath)) {
            throw new ResourceNotFoundException("File not found by path:" + preparedPath);
        }

        storageRepository.deleteResource(userId, preparedPath);
    }

    private void deleteDirectory(String path) {
        Long userId = userProvider.currentUserId();
        String preparedPath = pathPreparer.prepareDirectoryPath(path);

        // сделать обший validateResourceExists, который внутри сам будет определять папка это или файл и проверять
        directoryService.validateDirectoryExists(preparedPath);

        List<StorageObjectInfo> resources = storageRepository.findResourcesRecursiveByPrefix(userId, preparedPath);

        for(StorageObjectInfo resource : resources) {
            // тут как раз уже нужен обший validateResourceExists
        }
    }

    @Override
    public void validateFileNotExists(String filePath) {
        if (storageRepository.existsFile(userProvider.currentUserId(), filePath)) {
            throw new ResourceAlreadyExistsException("File already exists by path:" + filePath);
        }
    }


}
