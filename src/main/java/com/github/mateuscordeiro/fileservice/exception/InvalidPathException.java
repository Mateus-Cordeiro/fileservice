package com.github.mateuscordeiro.fileservice.exception;

public class InvalidPathException extends RuntimeException {
    public InvalidPathException(String message) {
        super("Invalid path: " + message);
    }
}
