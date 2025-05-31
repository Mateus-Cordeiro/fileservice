package com.github.mateuscordeiro.fileservice.util;

import java.nio.file.Path;

import com.github.mateuscordeiro.fileservice.config.RootPathProperties;
import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;

public class PathUtils {
    public static Path resolveSafePath(Path root, String path) {
        if (path == null) {
            throw new InvalidPathException("Path is blank or null");
        }
        Path resolved = root.resolve(path).normalize();
        if (!resolved.startsWith(root)) {
            throw new InvalidPathException("Path escapes the root directory");
        }
        return resolved;
    }

    public static Path getRoot(RootPathProperties rootPathProperties) {
        return rootPathProperties.getRoot().toAbsolutePath().normalize();
    }
}
