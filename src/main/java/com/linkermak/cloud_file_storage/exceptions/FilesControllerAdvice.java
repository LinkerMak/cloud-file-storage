package com.linkermak.cloud_file_storage.exceptions;

import com.linkermak.cloud_file_storage.dto.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class FilesControllerAdvice {

    private static final Logger log = LoggerFactory.getLogger(
            "FilesControllerAdvice"
    );

    @ExceptionHandler(InvalidPathException.class)
    ResponseEntity<ExceptionResponse> invalidPathHandler(InvalidPathException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    ResponseEntity<ExceptionResponse> directoryNotFoundHandler(DirectoryNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(DirectoryAlreadyExistsException.class)
    ResponseEntity<ExceptionResponse> directoryAlreadyExistsHandler(DirectoryAlreadyExistsException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<ExceptionResponse> storageHandler(StorageException e) {
        log.error("Storage exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Storage error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleSimpleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Unknown error"));
    }
}
