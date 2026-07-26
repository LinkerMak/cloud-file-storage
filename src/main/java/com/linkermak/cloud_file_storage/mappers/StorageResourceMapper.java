package com.linkermak.cloud_file_storage.mappers;

import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.services.path.StoragePathExtractor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StorageResourceMapper {

    public StorageResource toStorageResource(StorageObjectInfo objectInfo) {
        String resourcePath = objectInfo.path();

        boolean isDirectory = resourcePath.endsWith("/");

        return new StorageResource(
                StoragePathExtractor.extractParentPath(resourcePath).orElse(""),
                StoragePathExtractor.extractLastPath(resourcePath),
                isDirectory ? null : objectInfo.size(),
                isDirectory ? StorageResourceType.DIRECTORY : StorageResourceType.FILE
        );
    }

    public List<StorageResource> toStorageResources(List<StorageObjectInfo> objectInfoResources) {
        List<StorageResource> storageResources = new ArrayList<>();
        for (StorageObjectInfo objectInfo : objectInfoResources) {
            storageResources.add(
                    toStorageResource(objectInfo)
            );
        }

        return storageResources;
    }

    public StorageResource toDirectoryResource(String path) {
        return new StorageResource(
                StoragePathExtractor.extractParentPath(path).orElse(""),
                StoragePathExtractor.extractLastPath(path),
                null,
                StorageResourceType.DIRECTORY
        );
    }

    public StorageResource toFileResource(String path, long size) {
        return new StorageResource(
                StoragePathExtractor.extractParentPath(path).orElse(""),
                StoragePathExtractor.extractLastPath(path),
                size,
                StorageResourceType.FILE
        );
    }
}
