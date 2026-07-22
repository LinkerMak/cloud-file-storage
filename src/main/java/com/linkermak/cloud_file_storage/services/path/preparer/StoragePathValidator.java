package com.linkermak.cloud_file_storage.services.path.preparer;

import com.linkermak.cloud_file_storage.exceptions.InvalidPathException;

public final class StoragePathValidator {

    private StoragePathValidator() {
    }

    public static void validateFilePath(String path) {
        if (path.isBlank()) {
            throw new InvalidPathException("Directory path must end with '/'");
        }

        if (path.endsWith("/")) {
            throw new InvalidPathException("File path must not end with '/'");
        }
        validate(path);
    }

    public static void validateDirectoryPath(String path) {
        if (path.isEmpty()) {
            return;
        }

        if (!path.endsWith("/")) {
            throw new InvalidPathException("Directory path must end with '/'");
        }
        validate(path);
    }

    private static void validate(String path) {
        if (path.contains("\\")) {
            throw new InvalidPathException("Path must contain only '/' as separator");
        }

        if (path.startsWith("/")) {
            throw new InvalidPathException("Path must be relative");
        }

        if (path.contains("//")) {
            throw new InvalidPathException("Path must not contain duplicate '/'");
        }

        if (path.equals(".") || path.equals("..")) {
            throw new InvalidPathException("Path must not be '.' or '..'");
        }

        String[] segments = path.split("/");

        for (String segment : segments) {
            // TODO: возможно лучше isBlank(), надо потестить
            if (segment.isEmpty()) {
                continue;
            }

            if (segment.equals(".") || segment.equals("..")) {
                throw new InvalidPathException("Path must not contain '.' or '..' segments");
            }
        }
    }

}