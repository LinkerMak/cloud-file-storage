package com.linkermak.cloud_file_storage.services.zip;

import com.linkermak.cloud_file_storage.dto.repositories.storage.StorageObjectInfo;

import java.util.List;

public interface ZipArchiveService {

    byte[] createZip(Long userId, String directoryPath, List<StorageObjectInfo> resources);

}
