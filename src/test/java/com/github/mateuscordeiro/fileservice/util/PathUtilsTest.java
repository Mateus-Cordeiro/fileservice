package com.github.mateuscordeiro.fileservice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Path;

import com.github.mateuscordeiro.fileservice.config.RootPathProperties;
import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PathUtilsTest {
    @TempDir
    Path tempDir;

    @Test
    void resolveSafePath_whenPathIsInsideRoot_returnsResolvedPath() {
        Path resolved = PathUtils.resolveSafePath(tempDir, "subdir/file.txt");

        assertTrue(resolved.startsWith(tempDir));
        assertEquals(tempDir.resolve("subdir/file.txt").normalize(), resolved);
    }

    @Test
    void resolveSafePath_whenPathIsEmpty_returnsRoot() {
        Path resolved = PathUtils.resolveSafePath(tempDir, "");
        assertEquals(tempDir.normalize(), resolved);
    }

    @Test
    void resolveSafePath_whenPathIsNull_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> PathUtils.resolveSafePath(tempDir, null));
    }

    @Test
    void resolveSafePath_whenPathEscapesRoot_throwsInvalidPathException() {
        assertThrows(InvalidPathException.class, () -> PathUtils.resolveSafePath(tempDir, "../outside.txt"));
    }

    @Test
    void getRoot_returnsNormalizedAbsolutePath() {
        RootPathProperties props = new RootPathProperties();
        props.setRoot(tempDir);

        Path root = PathUtils.getRoot(props);

        assertEquals(tempDir.toAbsolutePath().normalize(), root);
    }
}
