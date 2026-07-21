package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.transfer.web.DownloadedResource;
import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.transfer.FileTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadResource(@RequestParam("path") String path) {
        System.out.println(path);
        DownloadedResource downloadedResource = fileTransferService.downloadResource(path);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .attachment()
                                .filename(downloadedResource.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentLength(downloadedResource.contentLength())
                .body(downloadedResource.resource());
    }
}
