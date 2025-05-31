package com.github.mateuscordeiro.fileservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.mateuscordeiro.fileservice.concurrency.FileConcurrencyManager;
import com.github.mateuscordeiro.fileservice.config.RootPathProperties;
import com.github.mateuscordeiro.fileservice.exception.FileAlreadyExistsException;
import com.github.mateuscordeiro.fileservice.exception.FileNotFoundException;
import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;
import com.github.mateuscordeiro.fileservice.io.FileIOManagerImpl;
import com.github.mateuscordeiro.fileservice.rpc.dto.FileInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileSystemServiceImplTest {
    @TempDir
    Path tempDir;
    private FileSystemServiceImpl service;

    @BeforeEach
    void setup() {
        RootPathProperties props = new RootPathProperties();
        props.setRoot(tempDir);

        service = new FileSystemServiceImpl(props, new FileIOManagerImpl(), new FileConcurrencyManager());
    }

    @Test
    void getFileInfo_whenFileExists_returnsCorrectInfo() throws IOException {
        Path file = tempDir.resolve("file.txt");
        Files.writeString(file, "hello world");

        FileInfo info = service.getFileInfo("file.txt");

        assertEquals("file.txt", info.getName());
        assertEquals("file.txt", info.getPath());
        assertTrue(info.getSize() > 0);
        assertFalse(info.isDirectory());
    }

    @Test
    void getFileInfo_whenDirectoryExists_returnsCorrectInfo() throws IOException {
        Path dir = tempDir.resolve("subdir");
        Files.createDirectory(dir);

        FileInfo info = service.getFileInfo("subdir");

        assertEquals("subdir", info.getName());
        assertEquals("subdir", info.getPath());
        assertEquals(0, info.getSize());
        assertTrue(info.isDirectory());
    }

    @Test
    void getFileInfo_whenFileIsEmpty_returnsZeroSize() throws IOException {
        Path file = tempDir.resolve("empty.txt");
        Files.createFile(file);

        FileInfo info = service.getFileInfo("empty.txt");

        assertEquals(0, info.getSize());
        assertFalse(info.isDirectory());
    }

    @Test
    void getFileInfo_whenNestedFileExists_returnsCorrectInfo() throws IOException {
        Path nestedDir = tempDir.resolve("a/b");
        Files.createDirectories(nestedDir);
        Path file = nestedDir.resolve("c.txt");
        Files.writeString(file, "test");

        FileInfo info = service.getFileInfo("a/b/c.txt");

        assertEquals("c.txt", info.getName());
        assertEquals("a/b/c.txt", info.getPath());
        assertTrue(info.getSize() > 0);
        assertFalse(info.isDirectory());
    }

    @Test
    void getFileInfo_whenFileDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.getFileInfo("missing.txt"));
    }

    @Test
    void getFileInfo_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.getFileInfo("../outside.txt"));
    }

    @Test
    void listChildren_whenDirectoryHasFiles_returnsFileInfoList() throws IOException {
        Path dir = tempDir.resolve("docs");
        Files.createDirectory(dir);
        Files.writeString(dir.resolve("a.txt"), "A");
        Files.writeString(dir.resolve("b.txt"), "B");

        List<FileInfo> children = service.listChildren("docs");

        assertEquals(2, children.size());
        assertTrue(children.stream().anyMatch(f -> f.getName().equals("a.txt")));
        assertTrue(children.stream().anyMatch(f -> f.getName().equals("b.txt")));
    }

    @Test
    void listChildren_whenDirectoryIsEmpty_returnsEmptyList() throws IOException {
        Path dir = tempDir.resolve("empty");
        Files.createDirectory(dir);

        List<FileInfo> children = service.listChildren("empty");

        assertTrue(children.isEmpty());
    }

    @Test
    void listChildren_whenDirectoryDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.listChildren("nope"));
    }

    @Test
    void listChildren_whenPathIsNotADirectory_throwsInvalidPathException() throws IOException {
        Path file = tempDir.resolve("notadir.txt");
        Files.writeString(file, "not a dir");

        assertThrows(InvalidPathException.class, () -> service.listChildren("notadir.txt"));
    }

    @Test
    void listChildren_whenPathIsRoot_returnsTopLevelChildren() throws IOException {
        Files.createDirectory(tempDir.resolve("folder"));
        Files.createFile(tempDir.resolve("file.txt"));

        List<FileInfo> children = service.listChildren("");

        assertEquals(2, children.size());
        assertTrue(children.stream().anyMatch(f -> f.getName().equals("folder")));
        assertTrue(children.stream().anyMatch(f -> f.getName().equals("file.txt")));
    }

    @Test
    void create_whenPathIsFile_createsFile() {
        String path = "file.txt";
        service.create(path, false);

        Path file = tempDir.resolve(path);
        assertTrue(Files.exists(file));
        assertFalse(Files.isDirectory(file));
    }

    @Test
    void create_whenPathIsDirectory_createsDirectory() {
        String path = "mydir";
        service.create(path, true);

        Path dir = tempDir.resolve(path);
        assertTrue(Files.exists(dir));
        assertTrue(Files.isDirectory(dir));
    }

    @Test
    void create_whenParentFoldersAreMissing_createsThem() {
        String path = "a/b/c/file.txt";
        service.create(path, false);

        Path file = tempDir.resolve(path);
        assertTrue(Files.exists(file));
        assertFalse(Files.isDirectory(file));
        assertTrue(Files.exists(file.getParent()));
    }

    @Test
    void create_whenPathAlreadyExists_throwsFileAlreadyExistsException() throws IOException {
        Path file = tempDir.resolve("existing.txt");
        Files.writeString(file, "data");

        assertThrows(FileAlreadyExistsException.class, () -> service.create("existing.txt", false));
    }

    @Test
    void create_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.create("../escape.txt", false));
    }

    @Test
    void create_whenPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.create("", false));
    }

    @Test
    void delete_whenFileExists_deletesFile() throws IOException {
        Path file = tempDir.resolve("to-delete.txt");
        Files.writeString(file, "temporary data");

        assertTrue(Files.exists(file));

        service.delete("to-delete.txt");

        assertFalse(Files.exists(file));
    }

    @Test
    void delete_whenDirectoryExists_deletesRecursively() throws IOException {
        Path nested = tempDir.resolve("dir/a/b/c.txt");
        Files.createDirectories(nested.getParent());
        Files.writeString(nested, "deep file");

        assertTrue(Files.exists(nested));

        service.delete("dir");

        assertFalse(Files.exists(tempDir.resolve("dir")));
    }

    @Test
    void delete_whenPathDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.delete("ghost.txt"));
    }

    @Test
    void delete_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.delete("../outside.txt"));
    }

    @Test
    void delete_whenPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.delete(""));
    }

    @Test
    void move_whenFileExists_movesToDestination() throws IOException {
        Path src = tempDir.resolve("a.txt");
        Files.writeString(src, "original");

        service.move("a.txt", "moved/a.txt");

        Path dest = tempDir.resolve("moved/a.txt");
        assertTrue(Files.exists(dest));
        assertFalse(Files.exists(src));
        assertEquals("original", Files.readString(dest));
    }

    @Test
    void move_whenDirectoryExists_movesRecursively() throws IOException {
        Path nested = tempDir.resolve("dir/sub/file.txt");
        Files.createDirectories(nested.getParent());
        Files.writeString(nested, "nested data");

        service.move("dir", "dir-moved");

        Path movedFile = tempDir.resolve("dir-moved/sub/file.txt");
        assertTrue(Files.exists(movedFile));
        assertFalse(Files.exists(tempDir.resolve("dir")));
        assertEquals("nested data", Files.readString(movedFile));
    }

    @Test
    void move_whenSourceDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.move("ghost.txt", "anywhere.txt"));
    }

    @Test
    void move_whenSourcePathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.move("../outside.txt", "safe.txt"));
    }

    @Test
    void move_whenDestinationPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.move("safe.txt", "../outside.txt"));
    }

    @Test
    void move_whenSourcePathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.move("", "dest.txt"));
    }

    @Test
    void move_whenDestinationPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.move("source.txt", ""));
    }

    @Test
    void move_whenDestinationFileExists_overwritesFile() throws IOException {
        Path src = tempDir.resolve("fileA.txt");
        Path dest = tempDir.resolve("fileB.txt");

        Files.writeString(src, "original A");
        Files.writeString(dest, "original B");

        service.move("fileA.txt", "fileB.txt");

        assertTrue(Files.exists(dest));
        assertFalse(Files.exists(src));
        assertEquals("original A", Files.readString(dest));
    }

    @Test
    void move_whenDestinationDirectoryIsNonEmpty_throwsFileAlreadyExistsException() throws IOException {
        Path srcDir = tempDir.resolve("dir1/sub");
        Path destDir = tempDir.resolve("dir2");

        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("a.txt"), "data");

        Files.createDirectories(destDir);
        Files.writeString(destDir.resolve("old.txt"), "to be replaced");

        assertThrows(FileAlreadyExistsException.class, () -> service.move("dir1", "dir2"));
    }

    @Test
    void copy_whenFileExists_copiesToDestination() throws IOException {
        Path src = tempDir.resolve("source.txt");
        Files.writeString(src, "hello");

        service.copy("source.txt", "copied.txt");

        Path dest = tempDir.resolve("copied.txt");
        assertTrue(Files.exists(dest));
        assertEquals("hello", Files.readString(dest));
    }

    @Test
    void copy_whenDirectoryExists_copiesRecursively() throws IOException {
        Path nested = tempDir.resolve("dir/a/b/c.txt");
        Files.createDirectories(nested.getParent());
        Files.writeString(nested, "deep");

        service.copy("dir", "copied-dir");

        Path copiedFile = tempDir.resolve("copied-dir/a/b/c.txt");
        assertTrue(Files.exists(copiedFile));
        assertEquals("deep", Files.readString(copiedFile));
    }

    @Test
    void copy_whenSourceDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.copy("ghost.txt", "anywhere.txt"));
    }

    @Test
    void copy_whenSourcePathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.copy("../outside.txt", "safe.txt"));
    }

    @Test
    void copy_whenDestinationPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.copy("safe.txt", "../outside.txt"));
    }

    @Test
    void copy_whenSourcePathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.copy("", "dest.txt"));
    }

    @Test
    void copy_whenDestinationPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.copy("source.txt", ""));
    }

    @Test
    void copy_whenDestinationFileExists_overwritesFile() throws IOException {
        Path src = tempDir.resolve("original.txt");
        Path dest = tempDir.resolve("existing.txt");

        Files.writeString(src, "from source");
        Files.writeString(dest, "old content");

        service.copy("original.txt", "existing.txt");

        assertEquals("from source", Files.readString(dest));
    }

    @Test
    void copy_whenDestinationDirectoryIsNonEmpty_throwsFileAlreadyExistsException() throws IOException {
        Path srcDir = tempDir.resolve("dir1/sub");
        Path destDir = tempDir.resolve("dir2");

        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("a.txt"), "data");

        Files.createDirectories(destDir);
        Files.writeString(destDir.resolve("existing.txt"), "preserve");

        assertThrows(FileAlreadyExistsException.class, () -> service.copy("dir1", "dir2"));
    }

    @Test
    void read_whenValidFile_returnsCorrectBytes() throws IOException {
        Path file = tempDir.resolve("data.txt");
        Files.writeString(file, "abcdefghij");

        String result = service.read("data.txt", 2, 4);

        assertEquals("cdef", result);
    }

    @Test
    void read_whenOffsetIsZero_readsFromStart() throws IOException {
        Path file = tempDir.resolve("alpha.txt");
        Files.writeString(file, "xyz");

        String result = service.read("alpha.txt", 0, 2);

        assertEquals("xy", result);
    }

    @Test
    void read_whenLengthExceedsEOF_returnsUpToEOF() throws IOException {
        Path file = tempDir.resolve("short.txt");
        Files.writeString(file, "12345");

        String result = service.read("short.txt", 2, 10);

        assertEquals("345", result);
    }

    @Test
    void read_whenOffsetBeyondEOF_returnsEmptyArray() throws IOException {
        Path file = tempDir.resolve("eof.txt");
        Files.writeString(file, "123");

        String result = service.read("eof.txt", 10, 5);

        assertEquals(0, result.length());
    }

    @Test
    void read_whenFileDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.read("missing.txt", 0, 10));
    }

    @Test
    void read_whenPathIsDirectory_throwsFileNotFoundException() throws IOException {
        Path dir = tempDir.resolve("dir");
        Files.createDirectory(dir);

        assertThrows(InvalidPathException.class, () -> service.read("dir", 0, 10));
    }

    @Test
    void read_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.read("../outside.txt", 0, 10));
    }

    @Test
    void read_whenPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.read("", 0, 10));
    }

    @Test
    void append_whenFileExists_appendsData() throws IOException {
        Path file = tempDir.resolve("append.txt");
        Files.writeString(file, "Hello");

        service.append("append.txt", " World");

        String result = Files.readString(file);
        assertEquals("Hello World", result);
    }

    @Test
    void append_whenFileDoesNotExist_throwsFileNotFoundException() {
        assertThrows(FileNotFoundException.class, () -> service.append("ghost.txt", "data"));
    }

    @Test
    void append_whenPathIsDirectory_throwsFileNotFoundException() throws IOException {
        Path dir = tempDir.resolve("mydir");
        Files.createDirectory(dir);

        assertThrows(InvalidPathException.class, () -> service.append("mydir", "text"));
    }

    @Test
    void append_whenPathIsEmpty_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.append("", "data"));
    }

    @Test
    void append_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> service.append("../outside.txt", "data"));
    }

    @Test
    void append_whenCalledConcurrently_preservesDataIntegrity() throws Exception {
        int threadCount = 10;
        String content = "123";
        Path file = tempDir.resolve("concurrent.txt");
        Files.createFile(file);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                service.append("concurrent.txt", content);
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        String result = Files.readString(file);
        assertEquals(content.repeat(threadCount), result);
    }

    @Test
    void append_whenCalledOnDifferentFiles_runsInParallel() throws Exception {
        int threadCount = 5;
        String dataA = "A";
        String dataB = "B";

        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.txt"));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount * 2);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                service.append("file1.txt", dataA);
                latch.countDown();
            });
        }

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                service.append("file2.txt", dataB);
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        String resultA = Files.readString(tempDir.resolve("file1.txt"));
        String resultB = Files.readString(tempDir.resolve("file2.txt"));

        assertEquals(dataA.repeat(threadCount), resultA);
        assertEquals(dataB.repeat(threadCount), resultB);
    }
}
