package com.linkermak.cloud_file_storage.dto.transfer;

import org.springframework.web.multipart.MultipartFile;

public record PreparedFileUpload(MultipartFile source,
                                 String normalizedRelativePath) {
}
