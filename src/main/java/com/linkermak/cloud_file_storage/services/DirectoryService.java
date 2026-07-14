package com.linkermak.cloud_file_storage.services;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import com.linkermak.cloud_file_storage.dto.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.ResourceNotFoundException;
import com.linkermak.cloud_file_storage.repositories.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.repositories.StorageObjectInfo;
import com.linkermak.cloud_file_storage.security.services.userdetails.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DirectoryService {

    private final ObjectStorageRepository storageRepository;

    @Autowired
    public DirectoryService(ObjectStorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    public List<StorageResource> getResourcesByPath(String pathDirectory) {
        Long userId = currentUserId();

        if(!storageRepository.existsDirectory(userId, pathDirectory)) {
            throw new ResourceNotFoundException("Directory not found by path:" + pathDirectory);
        }

        List<StorageObjectInfo> objectInfoResources =
                storageRepository.findResourcesByPrefix(userId, pathDirectory);

        return generateStorageResources(objectInfoResources);
    }

    private List<StorageResource> generateStorageResources(List<StorageObjectInfo> objectInfoResources) {
        List<StorageResource> storageResources = new ArrayList<>();
        for(StorageObjectInfo resourceInfo : objectInfoResources) {
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


    @Transactional
    public StorageResource createDirectory(String pathDirectory) {
        Long userId = currentUserId();
        Optional<String> parentPath = extractParentPath(pathDirectory);

        if(parentPath.isPresent()
                && !storageRepository.existsDirectory(userId, parentPath.get()) ) {
            throw new ResourceNotFoundException("Parent directory not found by path:" + pathDirectory);
        }

        if (storageRepository.existsDirectory(userId, pathDirectory)) {
            throw new ResourceAlreadyExistsException("Directory already exists by path:" + pathDirectory);
        }

        storageRepository.createDirectory(userId, pathDirectory);
        return new StorageResource(
                parentPath.orElse(""),
                extractLastPath(pathDirectory),
                null,
                StorageResourceType.DIRECTORY
        );
    }

    private Long currentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if(principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUserId();
        }

        throw new IllegalStateException("Unsupported authentication principal");
    }

    private Optional<String> extractParentPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if(lastSlash < 0) {
            return Optional.empty();
        }

        return Optional.of(withoutTrailingSlash.substring(0, lastSlash + 1));
    }

    private String extractLastPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if(lastSlash < 0) {
            return withoutTrailingSlash;
        }

        return withoutTrailingSlash.substring(lastSlash + 1);
    }

    private String pathWithoutTrailingSlash(String path) {
        if(path == null || path.isBlank()) {
            return path;
        }

        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
