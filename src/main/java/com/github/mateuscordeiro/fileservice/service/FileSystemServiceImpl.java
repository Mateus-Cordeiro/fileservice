package com.github.mateuscordeiro.fileservice.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import com.github.mateuscordeiro.fileservice.concurrency.FileConcurrencyManager;
import com.github.mateuscordeiro.fileservice.config.RootPathProperties;
import com.github.mateuscordeiro.fileservice.exception.FileAlreadyExistsException;
import com.github.mateuscordeiro.fileservice.exception.FileNotFoundException;
import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;
import com.github.mateuscordeiro.fileservice.io.FileIOManager;
import com.github.mateuscordeiro.fileservice.rpc.dto.FileInfo;
import com.github.mateuscordeiro.fileservice.util.PathUtils;
import com.github.mateuscordeiro.fileservice.validation.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileSystemServiceImpl implements FileSystemService {
    private final RootPathProperties rootPathProperties;
    private final FileIOManager fileIOManager;
    private final FileConcurrencyManager fileConcurrencyManager;

    @Override
    public FileInfo getFileInfo(String path) {
        Path file = resolve(path);
        if (!fileIOManager.exists(file)) {
            throw new FileNotFoundException(path);
        }
        try {
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            return FileInfo.builder()
                           .name(file.getFileName().toString())
                           .path(PathUtils.getRoot(rootPathProperties).relativize(file).toString())
                           .size(attrs.isDirectory() ? 0L : attrs.size())
                           .directory(attrs.isDirectory())
                           .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file info: " + path, e);
        }
    }

    @Override
    public List<FileInfo> listChildren(String path) {
        Path directory = resolve(path);
        if (!fileIOManager.exists(directory)) {
            throw new FileNotFoundException(path);
        }
        if (!fileIOManager.isDirectory(directory)) {
            throw new InvalidPathException("Not a directory: " + path);
        }

        try (Stream<Path> stream = fileIOManager.listDirectory(directory)) {
            return stream
                    .map(p -> getFileInfo(PathUtils.getRoot(rootPathProperties).relativize(p).toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list children of: " + path, e);
        }
    }

    @Override
    public void create(String path, boolean isDirectory) {
        ValidationUtils.validateWritablePath(path);
        Path file = resolve(path);

        try {
            if (Files.exists(file)) {
                throw new FileAlreadyExistsException(path);
            }
            if (isDirectory) {
                fileIOManager.createDirectory(file);
            } else {
                if (file.getParent() != null) { // TODO: why?
                    fileIOManager.createDirectory(file.getParent());
                }
                fileIOManager.createFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        ValidationUtils.validateWritablePath(path);
        Path file = resolve(path);

        if (!fileIOManager.exists(file)) {
            throw new FileNotFoundException(path);
        }
        try {
            fileIOManager.deleteRecursively(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + path, e);
        }
    }

    @Override
    public void move(String source, String destination) {
        ValidationUtils.validateWritablePath(source);
        ValidationUtils.validateWritablePath(destination);

        Path sourceFile = resolve(source);
        Path destinationFile = resolve(destination);

        try {
            if (!fileIOManager.exists(sourceFile)) {
                throw new FileNotFoundException(source);
            }

            if (fileIOManager.isDirectory(destinationFile) && !fileIOManager.isEmptyDirectory(destinationFile)) {
                throw new FileAlreadyExistsException("Destination directory is not empty: " + destination);
            }

            if (destinationFile.getParent() != null) {
                fileIOManager.createDirectory(destinationFile.getParent());
            }
            fileIOManager.move(sourceFile, destinationFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move from " + source + " to " + destination, e);
        }
    }

    @Override
    public void copy(String source, String destination) {
        ValidationUtils.validateWritablePath(source);
        ValidationUtils.validateWritablePath(destination);

        Path sourceFile = resolve(source);
        Path destinationFile = resolve(destination);

        if (!fileIOManager.exists(sourceFile)) {
            throw new FileNotFoundException(source);
        }

        try {
            if (fileIOManager.isDirectory(destinationFile) && !fileIOManager.isEmptyDirectory(destinationFile)) {
                throw new FileAlreadyExistsException("Cannot overwrite non-empty directory: " + destinationFile);
            }

            if (destinationFile.getParent() != null) {
                fileIOManager.createDirectory(destinationFile.getParent());
            }

            if (fileIOManager.isDirectory(sourceFile)) {
                fileIOManager.copyDirectory(sourceFile, destinationFile); // Handle
            } else {
                fileIOManager.copy(sourceFile, destinationFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy from " + source + " to " + destination, e);
        }
    }

    @Override
    public void append(String path, String data) {
        ValidationUtils.validateWritablePath(path);
        Path file = resolve(path);

        if (!fileIOManager.exists(file)) {
            throw new FileNotFoundException(path);
        }
        if (fileIOManager.isDirectory(file)) {
            throw new InvalidPathException(path);
        }

        fileConcurrencyManager.withLock(file, () -> {
            try {
                fileIOManager.writeString(file, data);
            } catch (IOException e) {
                throw new RuntimeException("Failed to append to file: " + path, e);
            }
        });
    }

    @Override
    public String read(String path, int offset, int length) {
        ValidationUtils.validateWritablePath(path);
        Path file = resolve(path);

        if (!fileIOManager.exists(file)) {
            throw new FileNotFoundException(path);
        }
        if (fileIOManager.isDirectory(file)) {
            throw new InvalidPathException(path);
        }

        try {
            return fileIOManager.read(file, offset, length);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read from file: " + path, e);
        }
    }

    private Path resolve(String path) {
        Path root = PathUtils.getRoot(rootPathProperties);
        return PathUtils.resolveSafePath(root, path);
    }
}
