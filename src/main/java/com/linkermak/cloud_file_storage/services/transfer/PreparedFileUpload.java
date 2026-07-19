package com.linkermak.cloud_file_storage.services.transfer;

import org.springframework.web.multipart.MultipartFile;

public record PreparedFileUpload(MultipartFile source,
                                 String normalizedRelativePath) {
}
