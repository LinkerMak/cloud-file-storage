package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;

public interface ResourceService {

    StorageResource getResource(String path);

    void deleteResource(String path);

    void validateFileNotExists(String path);
}
