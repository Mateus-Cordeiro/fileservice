package com.github.mateuscordeiro.fileservice.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CreateRequest;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemCreateIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void createFile_createsFileSuccessfully() throws Exception {
        performRpc("create", CreateRequest.builder()
                                          .path("file.txt")
                                          .directory(false)
                                          .build()).andExpect(status().isOk());

        assertTrue(Files.exists(resolveTemp("file.txt")));
    }

    @Test
    void createDirectory_createsFolderSuccessfully() throws Exception {
        performRpc("create", CreateRequest.builder()
                                          .path("folder/sub")
                                          .directory(true)
                                          .build()).andExpect(status().isOk());

        Path created = resolveTemp("folder/sub");
        assertTrue(Files.exists(created));
        assertTrue(Files.isDirectory(created));
    }

    @Test
    void create_whenFileAlreadyExists_returnsError() throws Exception {
        writeTempFile("exists.txt", "already there");

        performRpc("create", CreateRequest.builder()
                                          .path("exists.txt")
                                          .directory(false)
                                          .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error.code").exists())
                .andExpect(jsonPath("$.error.message").value(Matchers.containsString("already exists")));
    }
}