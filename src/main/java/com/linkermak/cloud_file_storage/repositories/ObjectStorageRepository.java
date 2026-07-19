package com.linkermak.cloud_file_storage.repositories;

import java.util.List;

public interface ObjectStorageRepository {

    boolean existsFile(Long userId, String filePath);

    boolean existsDirectory(Long userId, String directoryPath);

    void createDirectory(Long userId, String directoryPath);

    List<StorageObjectInfo> findResourcesByPrefix(Long userId, String directoryPath);
}
