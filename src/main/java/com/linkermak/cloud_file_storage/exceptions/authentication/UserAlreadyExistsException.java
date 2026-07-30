package com.linkermak.cloud_file_storage.exceptions.authentication;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
