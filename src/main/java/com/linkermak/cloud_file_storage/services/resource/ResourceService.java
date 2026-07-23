package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;

import java.util.List;

public interface ResourceService {

    List<StorageResource> searchResources(String query);

    StorageResource getResource(String path);

    StorageResource moveResource(String from, String to);

    void deleteResource(String path);

    void validatePreparedFileNotExists(String preparedFilePath);

    void validatePreparedFileExists(String preparedFilePath);
}
