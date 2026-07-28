package com.linkermak.cloud_file_storage.controllers.resource;

import com.linkermak.cloud_file_storage.dto.web.controller.StorageResource;
import com.linkermak.cloud_file_storage.services.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController implements ResourceApi {

    private final ResourceService resourceService;

    @Override
    @GetMapping
    public ResponseEntity<StorageResource> getResourceInfoByPath(@RequestParam("path") String path) {
        StorageResource objectInfo = resourceService.getResource(path);
        return ResponseEntity
                .ok()
                .body(objectInfo);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteResource(@RequestParam("path") String path) {
        resourceService.deleteResource(path);
        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<List<StorageResource>> searchResources(@RequestParam("query") String query) {
        List<StorageResource> resources = resourceService.searchResources(query);
        return ResponseEntity
                .ok()
                .body(resources);
    }

    @Override
    @PostMapping("/move")
    public ResponseEntity<StorageResource> moveResource(@RequestParam("from") String from,
                                                        @RequestParam("to") String to) {
        StorageResource resource = resourceService.moveResource(from, to);
        return ResponseEntity
                .ok()
                .body(resource);
    }
}
