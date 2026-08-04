package com.linkermak.cloud_file_storage.exceptions.repository;

import com.linkermak.cloud_file_storage.repositories.storage.batch.result.BatchResult;

public class StorageException extends RuntimeException {

    private final BatchResult batchResult;

    public StorageException(String message) {
        super(message);
        this.batchResult = null;
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.batchResult = null;
    }

    public StorageException(String message, BatchResult batchResult) {
        super(message);
        this.batchResult = batchResult;
    }

    public StorageException(String message, Throwable cause, BatchResult batchResult) {
        super(message, cause);
        this.batchResult = batchResult;
    }

    public BatchResult getBatchResult() {
        return this.batchResult;
    }

    public boolean hasBatchResult() {
        return this.batchResult != null;
    }

}
