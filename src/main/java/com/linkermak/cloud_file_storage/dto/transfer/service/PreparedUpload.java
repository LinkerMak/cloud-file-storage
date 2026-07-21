package com.linkermak.cloud_file_storage.dto.transfer.service;

import java.util.List;

public record PreparedUpload(String normalizedDirectoryPath,
                             List<PreparedFileUpload> files) {
}
