package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CopyRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemCopyIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void copy_fileToNewLocation_copiesSuccessfully() throws Exception {
        writeTempFile("original.txt", "copy me");

        performRpc("copy", CopyRequest.builder()
                                      .source("original.txt")
                                      .destination("copy.txt")
                                      .build()).andExpect(status().isOk());

        assertTrue(Files.exists(resolveTemp("copy.txt")));
        assertEquals("copy me", Files.readString(resolveTemp("copy.txt")));
        assertTrue(Files.exists(resolveTemp("original.txt")));
    }

    @Test
    void copy_directoryWithFiles_copiesAllContents() throws Exception {
        writeTempFile("data/sub/file1.txt", "a");

        performRpc("copy", CopyRequest.builder()
                                      .source("data")
                                      .destination("backup")
                                      .build()).andExpect(status().isOk());

        assertTrue(Files.exists(resolveTemp("backup/sub/file1.txt")));
        assertEquals("a", Files.readString(resolveTemp("backup/sub/file1.txt")));
    }

    @Test
    void copy_createsDestinationParents_ifMissing() throws Exception {
        writeTempFile("config.yaml", "env=prod");

        performRpc("copy", CopyRequest.builder()
                                      .source("config.yaml")
                                      .destination("conf/envs/prod.yaml")
                                      .build()).andExpect(status().isOk());

        Path dest = resolveTemp("conf/envs/prod.yaml");
        assertTrue(Files.exists(dest));
        assertEquals("env=prod", Files.readString(dest));
    }

    @Test
    void copy_whenSourceDoesNotExist_returnsError() throws Exception {
        performRpc("copy", CopyRequest.builder()
                                      .source("ghost.txt")
                                      .destination("copy.txt")
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("not found")));
    }

    @Test
    void copy_whenDestinationFolderExistsAndIsNonEmpty_returnsError() throws Exception {
        writeTempFile("src/a.txt", "data");
        writeTempFile("dst/existing.txt", "should block");

        performRpc("copy", CopyRequest.builder()
                                      .source("src")
                                      .destination("dst")
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("already exists")));
    }
}