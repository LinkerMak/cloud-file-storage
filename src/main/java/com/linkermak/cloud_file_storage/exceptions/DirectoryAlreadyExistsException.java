package com.linkermak.cloud_file_storage.exceptions;

public class DirectoryAlreadyExistsException extends RuntimeException {
    public DirectoryAlreadyExistsException(String message) {
        super(message);
    }
}
