package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.StorageResource;
import com.linkermak.cloud_file_storage.services.FilesService;
import com.linkermak.cloud_file_storage.validator.StoragePathValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FilesController {

    private final FilesService filesService;

    @Autowired
    public FilesController(FilesService filesService) {
        this.filesService = filesService;
    }

    @PostMapping("/directory")
    ResponseEntity<StorageResource> createDirectory(@RequestParam("path") String path) {
        StoragePathValidator.validateDirectoryPath(path);
        StorageResource storageResponse = filesService.createDirectory(path);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storageResponse);
    }
}
