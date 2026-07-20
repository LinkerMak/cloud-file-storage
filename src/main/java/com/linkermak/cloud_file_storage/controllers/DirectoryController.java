package com.linkermak.cloud_file_storage.controllers;

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
public class DirectoryController {

    private final DirectoryService directoryService;

    @PostMapping
    ResponseEntity<StorageResource> createDirectory(@RequestParam("path") String path) {
        StorageResource storageResponse = directoryService.createDirectory(path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storageResponse);
    }

    @GetMapping
    ResponseEntity<List<StorageResource>> getAllDirectories(
            @RequestParam(value = "path", defaultValue = "") String path) {
        List<StorageResource> storageResources = directoryService.getResourcesByPath(path);
        return ResponseEntity
                .ok()
                .body(storageResources);
    }
}
