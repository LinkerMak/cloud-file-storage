package com.linkermak.cloud_file_storage.repositories;

import com.linkermak.cloud_file_storage.config.minio.MinioProperties;
import com.linkermak.cloud_file_storage.exceptions.StorageException;
import com.linkermak.cloud_file_storage.models.StorageResource;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;

@Repository
public class MinioObjectStorageRepository implements ObjectStorageRepository{

    private final MinioClient minioClient;
    private final String bucket;

    @Autowired
    public MinioObjectStorageRepository(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.bucket = properties.getBucket();
    }

    @Override
    public void createDirectory(Long userId, String directoryPath) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(pathToKey(userId, directoryPath))
                            .stream(new ByteArrayInputStream(new byte[0]), 0L, -1L)
                            .build()
            );
        } catch(Exception e) {
            throw new StorageException("Failed to create directory", e);
        }
    }

    private String pathToKey(Long id, String path) {
        return userRootPrefix(id) + path;
    }

    private String userRootPrefix(Long id) {
        return "user-" + id + "-files/";
    }
}
