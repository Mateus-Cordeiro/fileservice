package com.github.mateuscordeiro.fileservice.exception;

public class FileAlreadyExistsException extends RuntimeException {
    public FileAlreadyExistsException(String path) {
        super("File already exists: " + path);
    }
}