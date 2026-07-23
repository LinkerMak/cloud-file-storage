package com.linkermak.cloud_file_storage.exceptions;

import com.linkermak.cloud_file_storage.dto.web.exception.ExceptionResponse;
import com.linkermak.cloud_file_storage.exceptions.loader.DuplicateUploadResourceException;
import com.linkermak.cloud_file_storage.exceptions.loader.MultipartFileEmptyException;
import com.linkermak.cloud_file_storage.exceptions.loader.OriginalFileNameEmptyException;
import com.linkermak.cloud_file_storage.exceptions.repository.StorageException;
import com.linkermak.cloud_file_storage.exceptions.resources.InvalidPathException;
import com.linkermak.cloud_file_storage.exceptions.resources.InvalidQueryException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceAlreadyExistsException;
import com.linkermak.cloud_file_storage.exceptions.resources.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class FilesControllerAdvice {

    @ExceptionHandler(DuplicateUploadResourceException.class)
    public ResponseEntity<ExceptionResponse> DuplicateUploadResourceHandler(DuplicateUploadResourceException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(MultipartFileEmptyException.class)
    public ResponseEntity<ExceptionResponse> multipartFileEmptyHandler(MultipartFileEmptyException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(OriginalFileNameEmptyException.class)
    ResponseEntity<ExceptionResponse> originalFileNameEmptyHandler(OriginalFileNameEmptyException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(InvalidPathException.class)
    public ResponseEntity<ExceptionResponse> invalidPathHandler(InvalidPathException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(InvalidQueryException.class)
    public ResponseEntity<ExceptionResponse> invalidPathHandler(InvalidQueryException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> directoryNotFoundHandler(ResourceNotFoundException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ExceptionResponse("Directory not found"));
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> directoryAlreadyExistsHandler(ResourceAlreadyExistsException e) {
        warningLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionResponse("Directory already exists"));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ExceptionResponse> storageHandler(StorageException e) {
        errorLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Storage error"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleSimpleException(Exception e) {
        errorLogConsoleOutput(e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse("Unknown error"));
    }

    private void errorLogConsoleOutput(Exception e) {
        log.error("Error:reason = {}, message = {}", e, e.getMessage());
    }

    private void warningLogConsoleOutput(Exception e) {
        log.warn("Error:reason = {}, message = {}", e, e.getMessage());
    }
}
