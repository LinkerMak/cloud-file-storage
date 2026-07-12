package com.linkermak.cloud_file_storage.services;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import com.linkermak.cloud_file_storage.dto.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.DirectoryAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.DirectoryNotFoundException;
import com.linkermak.cloud_file_storage.repositories.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.security.services.userdetails.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FilesService {

    private final ObjectStorageRepository storageRepository;

    @Autowired
    public FilesService(ObjectStorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Transactional
    public StorageResource createDirectory(String pathDirectory) {
        Long userId = currentUserId();
        Optional<String> parentPath = extractParentPath(pathDirectory);

        if(parentPath.isPresent()
                && !storageRepository.existsDirectory(userId, parentPath.get()) ) {
            throw new DirectoryNotFoundException("Parent directory not found");
        }

        if (!storageRepository.existsDirectory(userId, pathDirectory)) {
            storageRepository.createDirectory(userId, pathDirectory);
            return new StorageResource(
                    parentPath.orElse(""),
                    extractLastPath(pathDirectory),
                    null,
                    StorageResourceType.DIRECTORY

            );
        }
        else throw new DirectoryAlreadyExistsException("Directory already exists");
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

        return Optional.of(withoutTrailingSlash.substring(0, lastSlash));
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
        return path.substring(0, path.length() - 1);
    }
}
