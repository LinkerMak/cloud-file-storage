package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.transfer.FileTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceLoaderController {

    private final FileTransferService fileTransferService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<StorageResource>> uploadResource(@RequestParam("path") String path,
                                                                @RequestParam("object") List<MultipartFile> files) throws IOException {
        List<StorageResource> storageResources = fileTransferService.uploadResource(path, files);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storageResources);
    }


}
