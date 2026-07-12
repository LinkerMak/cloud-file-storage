package com.linkermak.cloud_file_storage.repositories;

import com.linkermak.cloud_file_storage.config.minio.MinioProperties;
import com.linkermak.cloud_file_storage.exceptions.StorageException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public boolean existsDirectory(Long id, String directoryPath) {
        String key = pathToKey(id, directoryPath);

        if(objectExists(key)) {
            return true;
        }

        try{
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(key)
                            .maxKeys(1)
                            .recursive(true)
                            .build()
            );

            if(results.iterator().hasNext()) {
                return true;
            }

            return false;
        } catch(Exception e) {
            throw new StorageException("Failed to check object existence:" + key, e);
        }
    }

    private boolean objectExists(String key) {
        try{
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
            return true;
        } catch(ErrorResponseException e) {
            if("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check object existence:" + key, e);
        } catch(Exception e) {
            throw new StorageException("Failed to check object existence:" + key, e);
        }
    }

    private String pathToKey(Long id, String path) {
        return userRootPrefix(id) + path;
    }

    private String userRootPrefix(Long id) {
        return "user-" + id + "-files/";
    }
}
