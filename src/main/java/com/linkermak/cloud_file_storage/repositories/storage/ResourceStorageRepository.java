package com.linkermak.cloud_file_storage.repositories.storage;


import com.linkermak.cloud_file_storage.dto.repositories.storage.CopyPair;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;

import java.util.List;

public interface ResourceStorageRepository {

    StorageDownloadObject downloadFile(Long userId, String filePath);

    void uploadFile(UploadFileRequest fileRequest);

    boolean existsFile(Long userId, String filePath);

    void copy(Long userId, String from, String to);

    void copyMany(Long userId, List<CopyPair> copyPairs);

    void delete(Long userId, String path);

    void deleteMany(Long userId, List<String> paths);

    boolean existsDirectory(Long userId, String directoryPath);

    void ensureDirectoryExists(Long userId, String directoryPath);

    void createDirectory(Long userId, String directoryPath);

    StorageObjectInfo getInfo(Long userId, String path);

    List<StorageObjectInfo> findByPrefix(Long userId, String directoryPath);

    List<StorageObjectInfo> findDescendantsByPrefix(Long userId, String directoryPath);
}
