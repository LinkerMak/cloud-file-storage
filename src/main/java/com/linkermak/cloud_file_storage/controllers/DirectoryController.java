package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import com.linkermak.cloud_file_storage.services.DirectoryService;
import com.linkermak.cloud_file_storage.validator.StoragePathValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DirectoryController {

    private final DirectoryService directoryService;

    @Autowired
    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @PostMapping("/directory")
    ResponseEntity<StorageResource> createDirectory(@RequestParam("path") String path) {
        String normalizePath = StoragePathValidator.validateDirectoryPath(path);
        StorageResource storageResponse = directoryService.createDirectory(normalizePath);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storageResponse);
    }

    @GetMapping("/directory")
    ResponseEntity<List<StorageResource>> getAllDirectories(
            @RequestParam(value = "path", defaultValue = "") String path) {
        String normalizePath = StoragePathValidator.validateDirectoryPath(path);
        List<StorageResource> storageResources = directoryService.getResourcesByPath(normalizePath);
        return ResponseEntity
                .ok()
                .body(storageResources);
    }
}
