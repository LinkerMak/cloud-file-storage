package com.linkermak.cloud_file_storage.services.transfer;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileTransferService {

    List<StorageResource> uploadResource(String directoryPath, List<MultipartFile> files);
}
