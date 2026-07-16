package com.linkermak.cloud_file_storage.services;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import com.linkermak.cloud_file_storage.dto.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.repositories.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.repositories.StorageObjectInfo;
import com.linkermak.cloud_file_storage.services.authentication.userdetails.UserDetailsImpl;
import com.linkermak.cloud_file_storage.utils.StoragePathNormalizer;
import com.linkermak.cloud_file_storage.utils.StoragePathValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectoryServiceImpl implements DirectoryService {

    private final ObjectStorageRepository storageRepository;

    @Override
    public List<StorageResource> getResourcesByPath(String pathDirectory) {
        String normalizePath = StoragePathNormalizer.normalizeDirectoryPath(pathDirectory);
        StoragePathValidator.validateDirectoryPath(normalizePath);

        Long userId = currentUserId();

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
            String key = resourceInfo.key();

            boolean isDirectory = key.endsWith("/");

            storageResources.add(
                    new StorageResource(
                            extractParentPath(key).orElse(""),
                            extractLastPath(key),
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
        String normalizePath = StoragePathNormalizer.normalizeDirectoryPath(pathDirectory);
        StoragePathValidator.validateDirectoryPath(normalizePath);

        Long userId = currentUserId();
        Optional<String> parentPath = extractParentPath(normalizePath);

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
                extractLastPath(normalizePath),
                null,
                StorageResourceType.DIRECTORY
        );
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUserId();
        }

        throw new IllegalStateException("Unsupported authentication principal");
    }

    private Optional<String> extractParentPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if (lastSlash < 0) {
            return Optional.empty();
        }

        return Optional.of(withoutTrailingSlash.substring(0, lastSlash + 1));
    }

    private String extractLastPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if (lastSlash < 0) {
            return withoutTrailingSlash;
        }

        return withoutTrailingSlash.substring(lastSlash + 1);
    }

    private String pathWithoutTrailingSlash(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }

        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
