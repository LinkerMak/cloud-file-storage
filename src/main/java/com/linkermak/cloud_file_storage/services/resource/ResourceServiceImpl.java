package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.repositories.storage.CopyPair;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.exceptions.resources.InvalidPathException;
import com.linkermak.cloud_file_storage.exceptions.resources.InvalidQueryException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.mappers.StorageResourceMapper;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import com.linkermak.cloud_file_storage.services.path.preparer.StoragePathPreparer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private static final String BASE_DIRECTORY_PATH = "";

    private final DirectoryService directoryService;

    private final ResourceStorageRepository resourceStorageRepository;

    private final CurrentUserProvider userProvider;

    private final StoragePathPreparer pathPreparer;

    private final StorageResourceMapper resourceMapper;

    @Override
    public List<StorageResource> searchResources(String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidQueryException("Query in null");
        }
        String normalizedQuery = query.trim().toLowerCase();

        List<StorageObjectInfo> allUserResources = resourceStorageRepository.findDescendantsByPrefix(
                userProvider.currentUserId(),
                BASE_DIRECTORY_PATH
        );

        return allUserResources.stream()
                .filter(resource ->
                        resource.path().toLowerCase().contains(normalizedQuery))
                .sorted(
                        (a, b) ->
                            a.path().compareToIgnoreCase(b.path())
                )
                .map(resource ->
                        resourceMapper.toStorageResource(resource)
                )
                .toList();
    }

    @Override
    public StorageResource getResource(String path) {
        String trimmedPath = pathPreparer.trimPath(path);

        boolean isDirectory = trimmedPath.endsWith("/");

        String preparedPath = isDirectory ?
                pathPreparer.prepareDirectoryPath(trimmedPath) :
                pathPreparer.prepareFilePath(trimmedPath);

        if (isDirectory) {
            directoryService.validatePreparedDirectoryExists(preparedPath);
        } else {
            validatePreparedFileExists(preparedPath);
        }

        StorageObjectInfo objectInfo = resourceStorageRepository.getInfo(
                userProvider.currentUserId(),
                preparedPath
        );

        return resourceMapper.toStorageResource(objectInfo);
    }

    @Override
    public StorageResource moveResource(String from, String to) {
        String trimmedFrom = pathPreparer.trimPath(from);
        String trimmedTo = pathPreparer.trimPath(to);

        boolean isDirectory = trimmedFrom.endsWith("/");

        return isDirectory
                ? moveDirectory(trimmedFrom, trimmedTo)
                : moveFile(trimmedFrom, trimmedTo);
    }

    private record PreparedMove(String from, String to) {
    }

    private StorageResource moveFile(String from, String to) {
        PreparedMove preparedFileMove = prepareFileMove(from, to);

        Long userId = userProvider.currentUserId();

        resourceStorageRepository.copy(
                userId,
                preparedFileMove.from(),
                preparedFileMove.to()
        );

        validatePreparedFileExists(preparedFileMove.to());

        resourceStorageRepository.delete(userId, preparedFileMove.from());

        return getResource(preparedFileMove.to());
    }

    private PreparedMove prepareFileMove(String from, String to) {
        if (to.endsWith("/")) {
            throw new InvalidPathException(
                    "Trying to move file with path:" + from + " to directory path:" + to
            );
        }

        String preparedFromFilePath = pathPreparer.prepareFilePath(from);
        String preparedToFilePath = pathPreparer.prepareFilePath(to);

        validatePreparedFileExists(preparedFromFilePath);
        validatePreparedFileNotExists(preparedToFilePath);

        return new PreparedMove(preparedFromFilePath, preparedToFilePath);
    }

    private StorageResource moveDirectory(String from, String to) {
        PreparedMove preparedDirectoryMove = prepareDirectoryMove(from, to);

        Long userId = userProvider.currentUserId();

        List<CopyPair> copyPairs = buildCopyPairs(userId, preparedDirectoryMove);

        resourceStorageRepository.copyMany(userId, copyPairs);

        deleteCopiedPaths(userId, copyPairs);

        return resourceMapper.toDirectoryResource(preparedDirectoryMove.to());
    }

    private PreparedMove prepareDirectoryMove(String from, String to) {
        if (!to.endsWith("/")) {
            throw new InvalidPathException(
                    "Trying to move directory with path:" + from + " to file path:" + to
            );
        }

        String preparedFromDirectoryPath = pathPreparer.prepareDirectoryPath(from);
        String preparedToDirectoryPath = pathPreparer.prepareDirectoryPath(to);

        if (preparedToDirectoryPath.startsWith(preparedFromDirectoryPath)) {
            throw new InvalidPathException(
                    "To path starts with from path"
            );
        }

        directoryService.validatePreparedDirectoryExists(preparedFromDirectoryPath);
        directoryService.validatePreparedDirectoryNotExists(preparedToDirectoryPath);

        return new PreparedMove(preparedFromDirectoryPath, preparedToDirectoryPath);
    }

    private List<CopyPair> buildCopyPairs(Long userId, PreparedMove directoryMove) {
        List<StorageObjectInfo> storageResources = resourceStorageRepository.findDescendantsByPrefix(
                userId,
                directoryMove.from()
        );

        List<CopyPair> copyPairs = new ArrayList<>();
        for (StorageObjectInfo resource : storageResources) {
            String preparedNewResourcePath =
                    directoryMove.to()
                            + resource.path().substring(directoryMove.from().length());

            if (preparedNewResourcePath.endsWith("/")) {
                directoryService.validatePreparedDirectoryNotExists(preparedNewResourcePath);
            } else {
                validatePreparedFileNotExists(preparedNewResourcePath);
            }

            copyPairs.add(new CopyPair(resource.path(), preparedNewResourcePath));
        }
        copyPairs.add(new CopyPair(directoryMove.from(), directoryMove.to()));

        return copyPairs;
    }

    private void deleteCopiedPaths(Long userId, List<CopyPair> copyPairs) {
        List<String> pathsToDelete = copyPairs.stream()
                .map(copyPair -> copyPair.from())
                .toList();

        resourceStorageRepository.deleteMany(userId, pathsToDelete);
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

        resourceStorageRepository.delete(userId, preparedPath);
    }

    private void deleteDirectory(String directoryPath) {
        Long userId = userProvider.currentUserId();
        String preparedDirectoryPath = pathPreparer.prepareDirectoryPath(directoryPath);

        directoryService.validatePreparedDirectoryExists(preparedDirectoryPath);

        List<StorageObjectInfo> resources = resourceStorageRepository.findDescendantsByPrefix(userId, preparedDirectoryPath);

        List<String> pathsToDelete = new ArrayList<>(
                resources.stream()
                        .map(info -> info.path())
                        .toList());
        pathsToDelete.add(preparedDirectoryPath);

        resourceStorageRepository.deleteMany(
                userId,
                pathsToDelete
        );
    }

    @Override
    public void validatePreparedFileNotExists(String preparedFilePath) {
        if (resourceStorageRepository.existsFile(userProvider.currentUserId(), preparedFilePath)) {
            throw new ResourceAlreadyExistsException("File already exists by path:" + preparedFilePath);
        }
    }

    @Override
    public void validatePreparedFileExists(String preparedFilePath) {
        if (!resourceStorageRepository.existsFile(userProvider.currentUserId(), preparedFilePath)) {
            throw new ResourceNotFoundException("File not found by path:" + preparedFilePath);
        }
    }
}
