package com.linkermak.cloud_file_storage.repositories.storage;

import com.linkermak.cloud_file_storage.config.properties.MinioProperties;
import com.linkermak.cloud_file_storage.dto.repositories.storage.CopyPair;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.exceptions.repository.StorageException;
import com.linkermak.cloud_file_storage.repositories.storage.batch.result.BatchResult;
import io.minio.*;
import io.minio.messages.DeleteRequest;
import io.minio.messages.DeleteResult;
import io.minio.messages.Item;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("!test")
public class MinioResourceStorageRepository implements ResourceStorageRepository {

    private final MinioClient minioClient;

    private final String bucket;

    private final MinioOperationExecutor executor;

    public MinioResourceStorageRepository(MinioClient minioClient,
                                          MinioProperties properties,
                                          MinioOperationExecutor executor) {
        this.minioClient = minioClient;
        this.bucket = properties.getBucket();
        this.executor = executor;
    }

    @Override
    public StorageObjectInfo getInfo(Long userId, String resourcePath) {
        String key = pathToKey(userId, resourcePath);

        StatObjectResponse stat = executor.execute(
                () -> minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .build()
                ),
                "Failed to get resource information by key:" + key
        );

        return new StorageObjectInfo(
                resourcePath,
                stat.size()
        );
    }

    @Override
    public StorageDownloadObject downloadFile(Long userId, String path) {
        String key = pathToKey(userId, path);

        StatObjectResponse stat = executor.execute(
                () -> minioClient.statObject(
                        StatObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .build()),
                "Failed to download file by key:" + key
        );

        InputStream inputStream = executor.execute(
                () -> minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .build()),
                "Failed to download file by key:" + key
        );

        return new StorageDownloadObject(
                path,
                inputStream,
                stat.size()
        );
    }

    @Override
    public void uploadFile(UploadFileRequest fileRequest) {
        String key = pathToKey(fileRequest.userId(), fileRequest.filePath());

        executor.execute(
                () -> {
                    try (InputStream in = fileRequest.inputStream()) {
                        PutObjectArgs.Builder builder = PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .stream(in, fileRequest.size(), -1L)
                                .contentType(fileRequest.contentType());

                        minioClient.putObject(builder.build());
                    }
                },
                "Failed to upload file by key:" + key
        );
    }

    @Override
    public List<StorageObjectInfo> findDescendantsByPrefix(Long userId, String path) {
        return findResourcesByPrefix(userId, path, true);
    }

    @Override
    public List<StorageObjectInfo> findByPrefix(Long userId, String path) {
        return findResourcesByPrefix(userId, path, false);
    }

    public List<StorageObjectInfo> findResourcesByPrefix(Long userId, String path, boolean recursive) {
        String key = pathToKey(userId, path);

        Iterable<Result<Item>> results = executor.execute(
                () -> minioClient.listObjects(
                        ListObjectsArgs.builder()
                                .bucket(bucket)
                                .prefix(key)
                                .recursive(recursive)
                                .build()
                ),
                "Failed to find resources by key:" + key
        );

        return prepareResources(results, userId, key);
    }

    private List<StorageObjectInfo> prepareResources(Iterable<Result<Item>> results,
                                                     Long userId,
                                                     String key) {
        List<StorageObjectInfo> resources = new ArrayList<>();

        for (Result<Item> result : results) {
            try {
                Item item = result.get();

                if (item.objectName().equals(key)) {
                    continue;
                }

                String objectKey = item.objectName();
                String relativeKey = objectKey.substring(userRootPrefix(userId).length());
                resources.add(new StorageObjectInfo(relativeKey, item.size()));
            } catch (Exception e) {
                throw new StorageException("Failed to read listed resources by key:" + key, e);
            }
        }

        return resources;
    }

    @Override
    public void createDirectory(Long userId, String directoryPath) {
        String key = pathToKey(userId, directoryPath);
        executor.execute(
                () -> minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .stream(new ByteArrayInputStream(new byte[0]), 0L, -1L)
                                .build()),
                "Failed to create directory by key:" + key
        );
    }

    @Override
    public boolean existsFile(Long userId, String filePath) {
        String key = pathToKey(userId, filePath);
        return objectExists(key);
    }

    @Override
    public void copy(Long userId, String fromPath, String toPath) {
        String fromKey = pathToKey(userId, fromPath);
        String toKey = pathToKey(userId, toPath);
        executor.execute(
                () -> minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(toKey)
                                .source(
                                        SourceObject.builder()
                                                .bucket(bucket)
                                                .object(fromKey)
                                                .build()
                                )
                                .build()
                ),
                "Failed to copy file from path:" + fromPath + " to path:" + toPath
        );
    }

    @Override
    public void copyMany(Long userId, List<CopyPair> copyPairs) {
        BatchResult batchResult = checkCopyManyResults(userId, copyPairs);
        throwIfHasErrors(batchResult);
    }

    private BatchResult checkCopyManyResults(Long userId, List<CopyPair> copyPairs) {
        List<String> failedPairs = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (CopyPair copyPair : copyPairs) {
            try {
                copy(userId, copyPair.from(), copyPair.to());
            } catch (Exception e) {
                failedPairs.add(copyPair.from() + " -> " + copyPair.to());
                errorMessages.add(
                        "Failed to copy file from path: " + copyPair.from()
                                + " to path: " + copyPair.to()
                                + " , message:" + e.getMessage()
                );
            }
        }

        return createBatchResult(
                "copy",
                copyPairs.size(),
                failedPairs,
                errorMessages
        );
    }

    @Override
    public void delete(Long userId, String path) {
        String key = pathToKey(userId, path);
        executor.execute(
                () -> minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(key)
                                .build()
                ),
                "Failed to delete resource by key:" + key
        );
    }

    @Override
    public void deleteMany(Long userId, List<String> paths) {
        List<DeleteRequest.Object> objects = paths.stream()
                .map(path -> new DeleteRequest.Object(pathToKey(userId, path)))
                .toList();

        Iterable<Result<DeleteResult.Error>> errors = executor.execute(
                () -> minioClient.removeObjects(
                        RemoveObjectsArgs.builder()
                                .bucket(bucket)
                                .objects(objects)
                                .build()
                ),
                "Failed to delete resources"
        );

        throwIfHasErrors(
                checkRemoveResult(errors, paths)
        );
    }

    private BatchResult checkRemoveResult(Iterable<Result<DeleteResult.Error>> errors,
                                          List<String> requestedPaths) {
        List<String> errorMessages = new ArrayList<>();
        List<String> failedObjects = new ArrayList<>();

        for (Result<DeleteResult.Error> errorResult : errors) {
            try {
                DeleteResult.Error error = errorResult.get();
                failedObjects.add(error.resource());
                errorMessages.add(
                        "Failed to delete resource by key:" + error.resource()
                                + ", message:" + error.message()
                );
            } catch (Exception e) {
                errorMessages.add("Failed to read delete result: " + e.getMessage());
            }
        }

        return createBatchResult(
                "delete",
                requestedPaths.size(),
                failedObjects,
                errorMessages
        );
    }

    private BatchResult createBatchResult(
            String operation,
            int requestedCount,
            List<String> failedItems,
            List<String> errorMessages
    ) {
        int failedCount = failedItems.size();
        int successCount = requestedCount - failedCount;

        return new BatchResult(
                operation,
                requestedCount,
                successCount,
                failedCount,
                failedItems,
                errorMessages
        );
    }

    private void throwIfHasErrors(BatchResult batchResult) {
        if (batchResult.hasErrors()) {
            throw new StorageException("Batch operation failed", batchResult);
        }
    }

    @Override
    public boolean existsDirectory(Long id, String directoryPath) {
        String key = pathToKey(id, directoryPath);

        if (key.equals(userRootPrefix(id))) {
            return true;
        }

        if (objectExists(key)) {
            return true;
        }

        return executor.execute(
                () -> {
                    Iterable<Result<Item>> results = minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(bucket)
                                    .prefix(key)
                                    .maxKeys(1)
                                    .recursive(true)
                                    .build()
                    );

                    return results.iterator().hasNext();
                },
                "Failed to check object existence:" + key
        );
    }

    private boolean objectExists(String key) {
        return executor.executeBoolean(
                () -> {
                    minioClient.statObject(
                            StatObjectArgs.builder()
                                    .bucket(bucket)
                                    .object(key)
                                    .build()
                    );
                    return true;
                },
                "Failed to check object existence by key:" + key
        );
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
