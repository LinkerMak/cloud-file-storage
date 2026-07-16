package com.linkermak.cloud_file_storage.services;

import com.linkermak.cloud_file_storage.dto.StorageResource;

import java.util.List;

public interface DirectoryService {

    List<StorageResource> getResourcesByPath(String pathDirectory);

    StorageResource createDirectory(String pathDirectory);
}
