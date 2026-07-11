package com.linkermak.cloud_file_storage.repositories;

import io.minio.errors.MinioException;

public interface ObjectStorageRepository {
    void createDirectory(Long userId, String key) throws MinioException;
}
