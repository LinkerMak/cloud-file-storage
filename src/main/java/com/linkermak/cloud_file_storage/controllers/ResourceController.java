package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<StorageResource> getResourceByPath(@RequestParam("path") String path) {
        StorageResource objectInfo = resourceService.getResource(path);
        return ResponseEntity
                .ok()
                .body(objectInfo);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam("path") String path) {
        resourceService.deleteResource(path);
        return ResponseEntity
                .noContent()
                .build();
    }
}
