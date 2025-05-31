package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ListChildrenRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemListChildrenIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void listChildren_whenDirectoryHasFiles_returnsChildren() throws Exception {
        writeTempFile("folder/file1.txt", "a");
        writeTempFile("folder/file2.txt", "b");

        performRpc("listChildren", ListChildrenRequest.builder()
                                                      .path("folder")
                                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(2)))
                .andExpect(jsonPath("$.result[*].name", containsInAnyOrder("file1.txt", "file2.txt")));
    }

    @Test
    void listChildren_whenDirectoryIsEmpty_returnsEmptyList() throws Exception {
        createTempFolder("empty");

        performRpc("listChildren", ListChildrenRequest.builder()
                                                      .path("empty")
                                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", hasSize(0)));
    }

    @Test
    void listChildren_whenPathIsNotDirectory_returnsError() throws Exception {
        writeTempFile("notadir.txt", "data");

        performRpc("listChildren", ListChildrenRequest.builder()
                                                      .path("notadir.txt")
                                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("Not a directory")));
    }

    @Test
    void listChildren_whenPathDoesNotExist_returnsError() throws Exception {
        performRpc("listChildren", ListChildrenRequest.builder()
                                                      .path("ghost")
                                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("not found")));
    }
}