package com.github.mateuscordeiro.fileservice.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface FileIOManager {
    void createFile(Path path) throws IOException;

    void createDirectory(Path path) throws IOException;

    String read(Path path, long offset, int length) throws IOException;

    void writeString(Path path, String data) throws IOException;

    void deleteRecursively(Path path) throws IOException;

    void copy(Path source, Path destination) throws IOException;

    void move(Path source, Path destination) throws IOException;

    boolean exists(Path path);

    boolean isDirectory(Path path);

    void copyDirectory(Path source, Path destination) throws IOException;

    boolean isEmptyDirectory(Path destinationFile) throws IOException;

    Stream<Path> listDirectory(Path path) throws IOException;
}