package com.linkermak.cloud_file_storage.services.path.preparer;

import com.linkermak.cloud_file_storage.exceptions.resources.InvalidPathException;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class StoragePathPreparer {

    public String trimPath(String path) {
        if (path == null) {
            throw new InvalidPathException("Path is null");
        }

        return path.trim();
    }

    public String prepareFilePath(String filePath) {
        return preparePath(
                filePath,
                StoragePathNormalizer::normalizeFilePath,
                StoragePathValidator::validateFilePath);
    }

    public String prepareDirectoryPath(String directoryPath) {
        return preparePath(
                directoryPath,
                StoragePathNormalizer::normalizeDirectoryPath,
                StoragePathValidator::validateDirectoryPath);
    }

    private String preparePath(String path,
                               Function<String, String> normalizer,
                               Consumer<String> validator) {
        path = normalizer.apply(path);
        validator.accept(path);
        return path;
    }

}
