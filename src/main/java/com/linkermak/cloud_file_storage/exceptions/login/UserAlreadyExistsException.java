package com.linkermak.cloud_file_storage.exceptions.login;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
