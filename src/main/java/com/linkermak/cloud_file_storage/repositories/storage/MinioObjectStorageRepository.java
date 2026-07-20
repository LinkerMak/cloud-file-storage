package com.linkermak.cloud_file_storage.repositories.storage;

import com.linkermak.cloud_file_storage.config.properties.MinioProperties;
import com.linkermak.cloud_file_storage.dto.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.exceptions.StorageException;
import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MinioObjectStorageRepository implements ObjectStorageRepository {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioObjectStorageRepository(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.bucket = properties.getBucket();
    }

    @Override
    public void uploadFile(UploadFileRequest fileRequest) {
        String key = pathToKey(fileRequest.userId(), fileRequest.filePath());
        try (InputStream in = fileRequest.inputStream()) {
            PutObjectArgs.Builder builder = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(in, fileRequest.size(), -1L)
                    .contentType(fileRequest.contentType());

            minioClient.putObject(builder.build());
        } catch (Exception e) {
            throw new StorageException("Failed to upload file by key:" + key, e);
        }
    }

    @Override
    public List<StorageObjectInfo> findResourcesByPrefix(Long userId, String path) {
        String key = pathToKey(userId, path);
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(key)
                            .recursive(false)
                            .build()
            );

            List<StorageObjectInfo> resources = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();

                if (item.objectName().equals(key)) {
                    continue;
                }

                String objectKey = item.objectName();
                String relativeKey = objectKey.substring(userRootPrefix(userId).length());
                resources.add(new StorageObjectInfo(relativeKey, item.size()));
            }

            return resources;
        } catch (Exception e) {
            throw new StorageException("Failed to find resources by key:" + key, e);
        }
    }

    @Override
    public void createDirectory(Long userId, String directoryPath) {
        String key = pathToKey(userId, directoryPath);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(new ByteArrayInputStream(new byte[0]), 0L, -1L)
                            .build()
            );
        } catch (Exception e) {
            throw new StorageException("Failed to create directory by key:" + key, e);
        }
    }

    @Override
    public boolean existsFile(Long userId, String filePath) {
        String key = pathToKey(userId, filePath);
        return objectExists(key);
    }

    @Override
    public boolean existsDirectory(Long id, String directoryPath) {
        String key = pathToKey(id, directoryPath);

        if (objectExists(key)) {
            return true;
        }

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(key)
                            .maxKeys(1)
                            .recursive(true)
                            .build()
            );

            return results.iterator().hasNext();
        } catch (Exception e) {
            throw new StorageException("Failed to check object existence:" + key, e);
        }
    }

    private boolean objectExists(String key) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check object existence:" + key, e);
        } catch (Exception e) {
            throw new StorageException("Failed to check object existence:" + key, e);
        }
    }

    @Override
    public void ensureDirectoryExists(Long userId, String directoryPath) {
        createDirectory(userId, directoryPath);
    }

    private String pathToKey(Long id, String path) {
        return userRootPrefix(id) + path;
    }

    private String userRootPrefix(Long id) {
        return "user-" + id + "-files/";
    }
}
