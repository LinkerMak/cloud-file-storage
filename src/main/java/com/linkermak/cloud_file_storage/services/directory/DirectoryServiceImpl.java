package com.linkermak.cloud_file_storage.services.directory;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.mappers.StorageResourceMapper;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final ResourceStorageRepository resourceStorageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    private final StorageResourceMapper resourceMapper;

    @Override
    public List<StorageResource> getDirectoryContent(String directoryPath) {
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        validatePreparedDirectoryExists(preparedDirectoryPath);

        List<StorageObjectInfo> objectInfoResources =
                resourceStorageRepository.findByPrefix(userId, preparedDirectoryPath);

        return resourceMapper.toStorageResources(objectInfoResources);
    }

    @Override
    @Transactional
    public StorageResource createDirectory(String directoryPath) {
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        validateCreateDirectory(userId, preparedDirectoryPath);

        resourceStorageRepository.createDirectory(userId, preparedDirectoryPath);

        return resourceMapper.toDirectoryResource(preparedDirectoryPath);
    }

    private void validateCreateDirectory(Long userId, String preparedDirectoryPath) {
        Optional<String> parentPath = StoragePathExtractor.extractParentPath(preparedDirectoryPath);

        if (parentPath.isPresent()
                && !resourceStorageRepository.existsDirectory(userId, parentPath.get())) {
            throw new ResourceNotFoundException("Parent directory not found by path:" + preparedDirectoryPath);
        }

        if (resourceStorageRepository.existsDirectory(userId, preparedDirectoryPath)) {
            throw new ResourceAlreadyExistsException("Directory already exists by path:" + preparedDirectoryPath);
        }
    }

    @Override
    public void validatePreparedDirectoryExists(String preparedDirectoryPath) {
        if (!resourceStorageRepository.existsDirectory(userProvider.currentUserId(), preparedDirectoryPath)) {
            throw new ResourceNotFoundException("Directory not found by path:" + preparedDirectoryPath);
        }
    }

    @Override
    public void validatePreparedDirectoryNotExists(String preparedDirectoryPath) {
        if (resourceStorageRepository.existsDirectory(userProvider.currentUserId(), preparedDirectoryPath)) {
            throw new ResourceAlreadyExistsException("Directory already exists by path:" + preparedDirectoryPath);
        }
    }

}
