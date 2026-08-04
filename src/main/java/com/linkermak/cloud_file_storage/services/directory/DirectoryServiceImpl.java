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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final ResourceStorageRepository resourceStorageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    private final StorageResourceMapper resourceMapper;

    @Override
    public List<StorageResource> getDirectoryContent(String directoryPath) {
        log.debug("Get directory content started: directoryPath={}", directoryPath);

        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        validatePreparedDirectoryExists(preparedDirectoryPath);

        List<StorageObjectInfo> objectInfoResources =
                resourceStorageRepository.findByPrefix(userId, preparedDirectoryPath);

        List<StorageResource> result = resourceMapper.toStorageResources(objectInfoResources);

        log.debug("Get directory content completed: directoryPath={}, itemsCount={}",
                preparedDirectoryPath, result.size());

        return result;
    }

    @Override
    @Transactional
    public StorageResource createDirectory(String directoryPath) {
        log.info("Create directory started: directoryPath={}", directoryPath);

        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        Long userId = userProvider.currentUserId();

        validateCreateDirectory(userId, preparedDirectoryPath);

        resourceStorageRepository.createDirectory(userId, preparedDirectoryPath);

        StorageResource result = resourceMapper.toDirectoryResource(preparedDirectoryPath);

        log.info("Create directory completed: directoryPath={}", preparedDirectoryPath);
        return result;
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
