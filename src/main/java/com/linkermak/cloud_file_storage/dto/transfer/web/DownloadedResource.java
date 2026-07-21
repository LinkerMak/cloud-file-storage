package com.linkermak.cloud_file_storage.dto.transfer.web;

import org.springframework.core.io.Resource;

public record DownloadedResource(
        String filename,
        Resource resource,
        long contentLength
) {
}
