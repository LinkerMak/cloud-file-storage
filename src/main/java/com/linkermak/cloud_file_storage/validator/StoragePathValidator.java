package com.linkermak.cloud_file_storage.validator;

import com.linkermak.cloud_file_storage.exceptions.InvalidPathException;

public final class StoragePathValidator {

    private StoragePathValidator() {}
    public static String validateFilePath(String path) {
        String normalized = normalize(path);

        if (normalized.endsWith("/")) {
            throw new InvalidPathException("File path must not end with '/'");
        }

        return normalized;
    }

    public static String validateDirectoryPath(String path) {
        String normalized = normalize(path);

        if (!normalized.endsWith("/")) {
            throw new InvalidPathException("Directory path must end with '/'");
        }

        return normalized;
    }

    public static String validateResourcePath(String path) {
        return normalize(path);
    }

    private static String normalize(String path) {
        if (path == null) {
            throw new InvalidPathException("Path is null");
        }

        String normalized = path.trim();

        if (normalized.isEmpty()) {
            throw new InvalidPathException("Path is empty");
        }

        if (normalized.contains("\\")) {
            throw new InvalidPathException("Path must contain only '/' as separator");
        }

        if (normalized.startsWith("/")) {
            throw new InvalidPathException("Path must be relative");
        }

        if (normalized.contains("//")) {
            throw new InvalidPathException("Path must not contain duplicate '/'");
        }

        if (normalized.equals(".") || normalized.equals("..")) {
            throw new InvalidPathException("Path must not be '.' or '..'");
        }

        String[] segments = normalized.split("/");

        for (String segment : segments) {
            if (segment.equals(".") || segment.equals("..")) {
                throw new InvalidPathException("Path must not contain '.' or '..' segments");
            }

            if (segment.isBlank()) {
                throw new InvalidPathException("Path contains empty segment");
            }
        }

        return normalized;
    }
}
