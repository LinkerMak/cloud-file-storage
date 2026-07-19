package com.linkermak.cloud_file_storage.services.transfer;

import java.util.List;

public record PreparedUpload(String normalizedDirectoryPath,
                             List<PreparedFileUpload> files)  {
}
