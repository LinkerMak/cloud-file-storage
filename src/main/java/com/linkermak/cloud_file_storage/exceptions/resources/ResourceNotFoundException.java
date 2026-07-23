package com.linkermak.cloud_file_storage.exceptions.resources;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
