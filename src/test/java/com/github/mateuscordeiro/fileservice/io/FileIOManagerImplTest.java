package com.github.mateuscordeiro.fileservice.io;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileIOManagerImplTest {

    @TempDir
    Path tempDir;
    private FileIOManagerImpl manager;

    @BeforeEach
    void setUp() {
        manager = new FileIOManagerImpl();
    }

    @Test
    void createFile_createsFile() throws IOException {
        Path path = tempDir.resolve("file.txt");
        manager.createFile(path);
        assertTrue(Files.exists(path));
        assertFalse(Files.isDirectory(path));
    }

    @Test
    void createDirectory_createsNestedDirectories() throws IOException {
        Path path = tempDir.resolve("a/b/c");
        manager.createDirectory(path);
        assertTrue(Files.exists(path));
        assertTrue(Files.isDirectory(path));
    }

    @Test
    void writeBytes_and_readString_workCorrectly() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "initial");

        String appendData = " appended";
        manager.writeString(file, appendData);

        String result = manager.read(file, 0, 100);
        assertEquals("initial appended", result);
    }

    @Test
    void readBytes_whenOffsetPastEOF_returnsEmptyArray() throws IOException {
        Path file = tempDir.resolve("eof.txt");
        Files.writeString(file, "abc");

        String result = manager.read(file, 100, 10);
        assertEquals(0, result.length());
    }

    @Test
    void deleteRecursively_deletesAllContents() throws IOException {
        Path dir = tempDir.resolve("del/a/b");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("file.txt"), "data");

        Path root = tempDir.resolve("del");
        assertTrue(Files.exists(root));

        manager.deleteRecursively(root);

        assertFalse(Files.exists(root));
    }

    @Test
    void copy_copiesFileToTarget() throws IOException {
        Path src = tempDir.resolve("src.txt");
        Path dst = tempDir.resolve("dst.txt");
        Files.writeString(src, "copy this");

        manager.copy(src, dst);

        assertTrue(Files.exists(dst));
        assertEquals("copy this", Files.readString(dst));
    }

    @Test
    void move_movesFileToTarget() throws IOException {
        Path src = tempDir.resolve("move.txt");
        Path dst = tempDir.resolve("moved.txt");
        Files.writeString(src, "move me");

        manager.move(src, dst);

        assertTrue(Files.exists(dst));
        assertFalse(Files.exists(src));
        assertEquals("move me", Files.readString(dst));
    }

    @Test
    void exists_and_isDirectory_workCorrectly() throws IOException {
        Path file = tempDir.resolve("check.txt");
        Path dir = tempDir.resolve("check-dir");

        Files.writeString(file, "x");
        Files.createDirectory(dir);

        assertTrue(manager.exists(file));
        assertFalse(manager.isDirectory(file));

        assertTrue(manager.exists(dir));
        assertTrue(manager.isDirectory(dir));
    }

    @Test
    void copyDirectory_copiesAllContentsRecursively() throws IOException {
        Path source = tempDir.resolve("dir1/a/b");
        Files.createDirectories(source);
        Files.writeString(source.resolve("file.txt"), "nested");

        Path target = tempDir.resolve("copied-dir");

        manager.copyDirectory(tempDir.resolve("dir1"), target);

        Path copiedFile = target.resolve("a/b/file.txt");
        assertTrue(Files.exists(copiedFile));
        assertEquals("nested", Files.readString(copiedFile));
    }

    @Test
    void isEmptyDirectory_returnsTrueForEmptyDir() throws IOException {
        Path dir = tempDir.resolve("empty");
        Files.createDirectory(dir);

        assertTrue(manager.isEmptyDirectory(dir));
    }

    @Test
    void isEmptyDirectory_returnsFalseForNonEmptyDir() throws IOException {
        Path dir = tempDir.resolve("nonempty");
        Files.createDirectory(dir);
        Files.writeString(dir.resolve("file.txt"), "data");

        assertFalse(manager.isEmptyDirectory(dir));
    }
}
