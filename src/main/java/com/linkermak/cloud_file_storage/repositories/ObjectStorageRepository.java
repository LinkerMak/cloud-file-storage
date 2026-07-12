package com.linkermak.cloud_file_storage.repositories;

public interface ObjectStorageRepository {
    boolean existsDirectory(Long userId, String directoryPath);
    void createDirectory(Long userId, String directoryPath);
}
