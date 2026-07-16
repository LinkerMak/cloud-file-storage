package com.linkermak.cloud_file_storage.exceptions;

import com.linkermak.cloud_file_storage.dto.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FilesControllerAdvice {

    @ExceptionHandler(InvalidPathException.class)
    ResponseEntity<ExceptionResponse> invalidPathHandler(InvalidPathException e) {
        log.error(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ExceptionResponse> directoryNotFoundHandler(ResourceNotFoundException e) {
        log.error(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse("Directory not found"));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    ResponseEntity<ExceptionResponse> directoryAlreadyExistsHandler(ResourceAlreadyExistsException e) {
        log.error(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionResponse("Directory already exists"));
    }

    @ExceptionHandler(StorageException.class)
    ResponseEntity<ExceptionResponse> storageHandler(StorageException e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Storage error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleSimpleException(Exception e) {
        log.error(e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Unknown error"));
    }
}
