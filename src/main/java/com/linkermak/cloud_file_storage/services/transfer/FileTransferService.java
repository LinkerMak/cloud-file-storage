package com.linkermak.cloud_file_storage.services.transfer;

import com.linkermak.cloud_file_storage.dto.transfer.web.DownloadedResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileTransferService {

    List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files) throws IOException;

    DownloadedResource downloadResource(String path);
}
