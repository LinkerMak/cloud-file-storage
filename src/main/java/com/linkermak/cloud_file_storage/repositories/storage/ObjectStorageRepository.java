package com.linkermak.cloud_file_storage.repositories.storage;

import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;

import java.util.List;

public interface ObjectStorageRepository {

    StorageObjectInfo getResourceInfoByPath(Long userId, String path);

    StorageDownloadObject downloadFile(Long userId, String filePath);

    void uploadFile(UploadFileRequest fileRequest);

    boolean existsFile(Long userId, String filePath);

    void deleteResource(Long userId, String path);

    void deleteResources(Long userId, List<String> paths);

    boolean existsDirectory(Long userId, String directoryPath);

    void ensureDirectoryExists(Long userId, String directoryPath);

    void createDirectory(Long userId, String directoryPath);

    List<StorageObjectInfo> findResourcesByPrefix(Long userId, String directoryPath);

    List<StorageObjectInfo> findDescendantsByPrefix(Long userId, String directoryPath);
}
