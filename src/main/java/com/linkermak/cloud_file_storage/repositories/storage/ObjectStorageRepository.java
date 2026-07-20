package com.linkermak.cloud_file_storage.repositories.storage;

import com.linkermak.cloud_file_storage.dto.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.storage.UploadFileRequest;

import java.util.List;

public interface ObjectStorageRepository {

    void uploadFile(UploadFileRequest fileRequest);

    boolean existsFile(Long userId, String filePath);

    boolean existsDirectory(Long userId, String directoryPath);

    void ensureDirectoryExists(Long userId, String directoryPath);

    void createDirectory(Long userId, String directoryPath);

    List<StorageObjectInfo> findResourcesByPrefix(Long userId, String directoryPath);
}
