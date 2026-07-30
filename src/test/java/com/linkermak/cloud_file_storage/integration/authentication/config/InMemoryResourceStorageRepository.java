package com.linkermak.cloud_file_storage.integration.authentication.config;

import com.linkermak.cloud_file_storage.dto.repositories.storage.CopyPair;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageDownloadObject;
import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;
import com.linkermak.cloud_file_storage.dto.repositories.storage.UploadFileRequest;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;

import java.util.List;

public class InMemoryResourceStorageRepository implements ResourceStorageRepository {
    @Override
    public StorageDownloadObject downloadFile(Long userId, String filePath) {
        return null;
    }

    @Override
    public void uploadFile(UploadFileRequest fileRequest) {

    }

    @Override
    public boolean existsFile(Long userId, String filePath) {
        return false;
    }

    @Override
    public void copy(Long userId, String from, String to) {

    }

    @Override
    public void copyMany(Long userId, List<CopyPair> copyPairs) {

    }

    @Override
    public void delete(Long userId, String path) {

    }

    @Override
    public void deleteMany(Long userId, List<String> paths) {

    }

    @Override
    public boolean existsDirectory(Long userId, String directoryPath) {
        return false;
    }

    @Override
    public void ensureDirectoryExists(Long userId, String directoryPath) {

    }

    @Override
    public void createDirectory(Long userId, String directoryPath) {

    }

    @Override
    public StorageObjectInfo getInfo(Long userId, String path) {
        return null;
    }

    @Override
    public List<StorageObjectInfo> findByPrefix(Long userId, String directoryPath) {
        return List.of();
    }

    @Override
    public List<StorageObjectInfo> findDescendantsByPrefix(Long userId, String directoryPath) {
        return List.of();
    }
}
