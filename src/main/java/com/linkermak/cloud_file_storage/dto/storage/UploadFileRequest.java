package com.linkermak.cloud_file_storage.dto.storage;

import java.io.InputStream;

public record UploadFileRequest(Long userId,
                                String filePath,
                                InputStream inputStream,
                                long size,
                                String contentType) {
}
