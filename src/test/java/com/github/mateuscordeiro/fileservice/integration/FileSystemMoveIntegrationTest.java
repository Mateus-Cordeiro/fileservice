package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.file.Files;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.MoveRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemMoveIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void move_fileToNewLocation_movesSuccessfully() throws Exception {
        writeTempFile("original.txt", "test");

        performRpc("move", MoveRequest.builder()
                                      .source("original.txt")
                                      .destination("moved.txt")
                                      .build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("original.txt")));
        assertTrue(Files.exists(resolveTemp("moved.txt")));
    }

    @Test
    void move_folderWithContents_movesEntireDirectory() throws Exception {
        writeTempFile("dir/sub/child.txt", "content");

        performRpc("move", MoveRequest.builder()
                                      .source("dir")
                                      .destination("newdir")
                                      .build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("dir")));
        assertTrue(Files.exists(resolveTemp("newdir/sub/child.txt")));
    }

    @Test
    void move_createsDestinationParentDirectories() throws Exception {
        writeTempFile("file.txt", "hello");

        performRpc("move", MoveRequest.builder()
                                      .source("file.txt")
                                      .destination("nested/path/file.txt")
                                      .build()).andExpect(status().isOk());

        assertTrue(Files.exists(resolveTemp("nested/path/file.txt")));
    }

    @Test
    void move_whenSourceDoesNotExist_returnsError() throws Exception {
        performRpc("move", MoveRequest.builder()
                                      .source("ghost.txt")
                                      .destination("anywhere.txt")
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("not found")));
    }

    @Test
    void move_whenDestinationExists_overwritesFile() throws Exception {
        writeTempFile("a.txt", "original");
        writeTempFile("b.txt", "old content");

        performRpc("move", MoveRequest.builder()
                                      .source("a.txt")
                                      .destination("b.txt")
                                      .build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("a.txt")));
        String content = Files.readString(resolveTemp("b.txt"));
        assertEquals("original", content);
    }
}