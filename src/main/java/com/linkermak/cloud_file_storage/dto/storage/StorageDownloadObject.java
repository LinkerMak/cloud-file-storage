package com.linkermak.cloud_file_storage.dto.storage;

import java.io.InputStream;

public record StorageDownloadObject(String fileName,
                                    InputStream inputStream,
                                    Long size) {
}
