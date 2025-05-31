package com.github.mateuscordeiro.fileservice.validation;

import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;

public class ValidationUtils {
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    public static void requireNonNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    public static void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    public static void requireNonNegative(long value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }

    public static void validateWritablePath(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidPathException("Path cannot be empty for write operations");
        }
    }
}
