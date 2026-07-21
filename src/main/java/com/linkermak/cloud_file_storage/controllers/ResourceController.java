package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    public ResponseEntity<StorageResource> getResourceByPath(@RequestParam("path") String path) {
        StorageResource objectInfo = resourceService.getResourceByPath(path);
        return ResponseEntity
                .ok()
                .body(objectInfo);
    }

}
