package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.resources.InvalidQueryException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private static final String BASE_DIRECTORY_PATH = "";

    private final DirectoryService directoryService;

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    @Override
    public List<StorageResource> searchResources(String query) {
        if(query == null || query.isBlank()) {
            throw new InvalidQueryException("Query in null");
        }
        String normalizedQuery = query.trim().toLowerCase();

        List<StorageObjectInfo> allUserResources = storageRepository.findDescendantsByPrefix(
                userProvider.currentUserId(),
                BASE_DIRECTORY_PATH
        );

        return allUserResources.stream()
                .filter(resource -> resource.path().toLowerCase().contains(normalizedQuery))
                .map(resource -> new StorageResource(
                        StoragePathExtractor.extractParentPath(resource.path()).orElse(""),
                        StoragePathExtractor.extractLastPath(resource.path()),
                        resource.size(),
                        resource.path().endsWith("/") ?
                                StorageResourceType.DIRECTORY
                                : StorageResourceType.FILE
                ))
                .toList();
    }

    @Override
    public StorageResource getResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);

        boolean isDirectory = trimmedPath.endsWith("/");

        String preparedPath = isDirectory ?
                pathPreparer.prepareDirectoryPath(trimmedPath) :
                pathPreparer.prepareFilePath(trimmedPath);

        if(isDirectory) {
            directoryService.validatePreparedDirectoryExists(preparedPath);
        } else {
            validatePreparedFileExists(preparedPath);
        }

        StorageObjectInfo objectInfo = storageRepository.getResourceInfoByPath(
                userProvider.currentUserId(),
                preparedPath
        );

        return new StorageResource(
                StoragePathExtractor.extractParentPath(objectInfo.path()).orElse(""),
                StoragePathExtractor.extractLastPath(objectInfo.path()),
                isDirectory ? null : objectInfo.size(),
                isDirectory ? StorageResourceType.DIRECTORY : StorageResourceType.FILE
        );
    }

    @Override
    public StorageResource moveResource(String from, String to) {
        if(isRename(from, to)) {

        }

        return move();
    }

    private boolean isRename(String from, String to) {

    }

    private StorageResource move(String from, String to) {

    }

    @Override
    public void deleteResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);
        boolean isDirectory = trimmedPath.endsWith("/");

        if (isDirectory) {
            deleteDirectory(trimmedPath);
        } else {
            deleteFile(trimmedPath);
        }
    }

    private void deleteFile(String path) {
        Long userId = userProvider.currentUserId();
        String preparedPath = pathPreparer.prepareFilePath(path);

        validatePreparedFileExists(preparedPath);

        storageRepository.deleteResource(userId, preparedPath);
    }

    private void deleteDirectory(String directoryPath) {
        Long userId = userProvider.currentUserId();
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        directoryService.validatePreparedDirectoryExists(preparedDirectoryPath);

        List<StorageObjectInfo> resources = storageRepository.findDescendantsByPrefix(userId, preparedDirectoryPath);

        List<String> pathsToDelete = new ArrayList<>(
                resources.stream()
                .map(info -> info.path())
                .toList());
        pathsToDelete.add(preparedDirectoryPath);

        storageRepository.deleteResources(
                userId,
                pathsToDelete);
    }

    @Override
    public void validatePreparedFileNotExists(String preparedFilePath) {
        if (storageRepository.existsFile(userProvider.currentUserId(), preparedFilePath)) {
            throw new ResourceAlreadyExistsException("File already exists by path:" + preparedFilePath);
        }
    }

    @Override
    public void validatePreparedFileExists(String preparedFilePath) {
        if (!storageRepository.existsFile(userProvider.currentUserId(), preparedFilePath)) {
            throw new ResourceNotFoundException("File not found by path:" + preparedFilePath);
        }
    }
}
