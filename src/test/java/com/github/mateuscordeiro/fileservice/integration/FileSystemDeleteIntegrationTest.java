package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.file.Files;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.DeleteRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemDeleteIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void delete_existingFile_removesIt() throws Exception {
        writeTempFile("test.txt", "test");

        performRpc("delete", DeleteRequest.builder()
                                          .path("test.txt")
                                          .build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("test.txt")));
    }

    @Test
    void delete_existingDirectoryWithChildren_removesAll() throws Exception {
        writeTempFile("folder/sub/file.txt", "child");

        performRpc("delete", DeleteRequest.builder()
                                          .path("folder")
                                          .build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("folder")));
    }

    @Test
    void delete_whenPathDoesNotExist_returnsError() throws Exception {
        performRpc("delete", DeleteRequest.builder()
                                          .path("ghost.txt")
                                          .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("not found")));
    }
}
