package com.linkermak.cloud_file_storage.web.controllers;

import com.linkermak.cloud_file_storage.services.FilesService;
import com.linkermak.cloud_file_storage.web.dto.FilesResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/directory?path=path")
    ResponseEntity<FilesResponse> createDirectory(@RequestParam("path") String path) {

    }
}
