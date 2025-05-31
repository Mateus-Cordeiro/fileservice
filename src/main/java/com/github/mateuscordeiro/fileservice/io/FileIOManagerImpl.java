package com.github.mateuscordeiro.fileservice.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class FileIOManagerImpl implements FileIOManager {
    @Override
    public void createFile(Path path) throws IOException {
        Files.createFile(path);
    }

    @Override
    public void createDirectory(Path path) throws IOException {
        Files.createDirectories(path);
    }

    @Override
    public String read(Path path, long offset, int length) throws IOException {
        try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            channel.position(offset);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            int bytesRead = channel.read(buffer);
            if (bytesRead == -1) {
                return new String(new byte[0], StandardCharsets.UTF_8);
            }
            return new String(Arrays.copyOf(buffer.array(), bytesRead), StandardCharsets.UTF_8);
        }
    }

    @Override
    public void writeString(Path path, String data) throws IOException {
        Files.writeString(path, data, StandardOpenOption.APPEND);
    }

    @Override
    public void deleteRecursively(Path path) throws IOException {
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to delete: " + p, e);
                    }
                });
        }
    }

    @Override
    public void copy(Path source, Path destination) throws IOException {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public void move(Path source, Path destination) throws IOException {
        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    @Override
    public void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            for (Path sourcePath : (Iterable<Path>) stream::iterator) {
                Path relative = source.relativize(sourcePath);
                Path targetPath = target.resolve(relative);
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    @Override
    public boolean isEmptyDirectory(Path path) throws IOException {
        return !Files.list(path).findAny().isPresent();
    }

    @Override public Stream<Path> listDirectory(Path path) throws IOException {
        return Files.list(path);
    }
}