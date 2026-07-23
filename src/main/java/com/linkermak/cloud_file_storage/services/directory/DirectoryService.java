package com.linkermak.cloud_file_storage.services.directory;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;

import java.util.List;

public interface DirectoryService {

    List<StorageResource> getDirectoryContent(String directoryPath);

    StorageResource createDirectory(String directoryPath);

    void validatePreparedDirectoryExists(String preparedDirectoryPath);
}
