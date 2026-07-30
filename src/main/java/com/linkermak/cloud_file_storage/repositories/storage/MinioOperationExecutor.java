package com.linkermak.cloud_file_storage.repositories.storage;

import com.linkermak.cloud_file_storage.exceptions.repository.StorageException;
import io.minio.errors.ErrorResponseException;
import org.springframework.stereotype.Component;

@Component
public class MinioOperationExecutor {

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    public <T> T execute(ThrowingSupplier<T> action, String errorMessage) {
        try {
            return action.get();
        } catch (ErrorResponseException e) {
            throw wrapMinioException(errorMessage, e);
        } catch (Exception e) {
            throw new StorageException(errorMessage, e);
        }
    }

    public void execute(ThrowingRunnable action, String errorMessage) {
        try {
            action.run();
        } catch (ErrorResponseException e) {
            throw wrapMinioException(errorMessage, e);
        } catch (Exception e) {
            throw new StorageException(errorMessage, e);
        }
    }

    public boolean executeBoolean(ThrowingSupplier<Boolean> action, String errorMessage) {
        try {
            return action.get();
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new StorageException(errorMessage, e);
        } catch (Exception e) {
            throw new StorageException(errorMessage, e);
        }
    }

    private StorageException wrapMinioException(String errorMessage, ErrorResponseException e) {
        if ("NoSuchKey".equals(e.errorResponse().code())) {
            throw new StorageException(errorMessage.replace("Failed", "Resource not found"), e);
        }
        throw new StorageException(errorMessage, e);
    }
}
