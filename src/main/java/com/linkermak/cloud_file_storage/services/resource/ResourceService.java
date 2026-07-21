package com.linkermak.cloud_file_storage.services.resource;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;

public interface ResourceService {

    StorageResource getResourceByPath(String path);

    void validateResourceNotExists(String filePath);
}
