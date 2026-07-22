package com.linkermak.cloud_file_storage.services.path;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@UtilityClass
public class StoragePathExtractor {

    public static List<String> extractAllParentPaths(String path) {
        List<String> paths = new ArrayList<>();
        Optional<String> parentPath = extractParentPath(path);

        while (parentPath.isPresent()) {
            String value = parentPath.get();
            paths.add(value);
            parentPath = extractParentPath(value);
        }

        Collections.reverse(paths);
        return paths;
    }

    public static Optional<String> extractParentPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if (lastSlash < 0) {
            return Optional.empty();
        }

        return Optional.of(withoutTrailingSlash.substring(0, lastSlash + 1));
    }

    public static String extractLastPath(String path) {
        String withoutTrailingSlash = pathWithoutTrailingSlash(path);
        int lastSlash = withoutTrailingSlash.lastIndexOf("/");

        if (lastSlash < 0) {
            return withoutTrailingSlash;
        }

        return withoutTrailingSlash.substring(lastSlash + 1);
    }

    private static String pathWithoutTrailingSlash(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }

        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
