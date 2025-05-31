package com.github.mateuscordeiro.fileservice.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String path) {
        super("File not found: " + path);
    }
}