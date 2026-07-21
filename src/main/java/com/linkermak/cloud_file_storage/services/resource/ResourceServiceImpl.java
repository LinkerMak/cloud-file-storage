package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.config.security.CurrentUserProvider;
import com.linkermak.cloud_file_storage.dto.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResourceType;
import com.linkermak.cloud_file_storage.exceptions.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.repositories.storage.ObjectStorageRepository;
import com.linkermak.cloud_file_storage.utils.StoragePathNormalizer;
import com.linkermak.cloud_file_storage.utils.StoragePathUtils;
import com.linkermak.cloud_file_storage.utils.StoragePathValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final ObjectStorageRepository storageRepository;

    private final CurrentUserProvider userProvider;

    @Override
    public StorageResource getResourceByPath(String path) {
        StorageObjectInfo objectInfo = storageRepository.getResourceByPath(
                userProvider.currentUserId(),
                prepareFilePath(path)
        );

        // TODO: только для файлов, а надо и для папок
        return new StorageResource(
                StoragePathUtils.extractParentPath(objectInfo.path()).orElse(""),
                StoragePathUtils.extractLastPath(objectInfo.path()),
                objectInfo.size(),
                StorageResourceType.FILE
        );
    }

    @Override
    public void validateResourceNotExists(String filePath) {
        if (storageRepository.existsFile(userProvider.currentUserId(), filePath)) {
            throw new ResourceAlreadyExistsException("File already exists by path:" + filePath);
        }
    }

    private String prepareFilePath(String filePath) {
        String normalizeFilePath = StoragePathNormalizer.normalizeFilePath(filePath);
        StoragePathValidator.validateFilePath(normalizeFilePath);
        return normalizeFilePath;
    }

}
