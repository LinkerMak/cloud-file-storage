package com.linkermak.cloud_file_storage.utils;

import com.linkermak.cloud_file_storage.exceptions.InvalidPathException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StoragePathNormalizer {

    public static String normalizeDirectoryPath(String path) {
        return normalize(path, true);
    }

    public static String normalizeFilePath(String path) {
        return normalize(path, false);
    }

    private static String normalize(String path, boolean isDirectory) {
        if (path == null) {
            throw new InvalidPathException("Path is null");
        }

        String normalized = path.trim();

        if (normalized.isEmpty()) {
            if (isDirectory) {
                return "";
            }
            throw new InvalidPathException("Path is empty");
        }

        return normalized;
    }

}
