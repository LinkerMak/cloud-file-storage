package com.linkermak.cloud_file_storage.controllers.directory;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.directory.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
public class DirectoryController implements DirectoryApi {

    private final DirectoryService directoryService;

    @Override
    @PostMapping
    public ResponseEntity<StorageResource> createDirectory(@RequestParam("path") String path) {
        StorageResource storageResponse = directoryService.createDirectory(path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storageResponse);
    }

    @Override
    @GetMapping
    public ResponseEntity<List<StorageResource>> getAllResourcesInDirectory(
            @RequestParam(value = "path", defaultValue = "") String path) {
        List<StorageResource> storageResources = directoryService.getDirectoryContent(path);
        return ResponseEntity
                .ok()
                .body(storageResources);
    }
}
