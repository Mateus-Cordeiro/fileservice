package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ReadRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemReadIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void read_fullFile_returnsCorrectData() throws Exception {
        writeTempFile("data.txt", "Hello World");

        performRpc("read", ReadRequest.builder()
                                      .path("data.txt")
                                      .offset(0)
                                      .length(11)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is("Hello World")));
    }

    @Test
    void read_partialData_withOffset_returnsCorrectChunk() throws Exception {
        writeTempFile("chunk.txt", "abcdefghij");

        performRpc("read", ReadRequest.builder()
                                      .path("chunk.txt")
                                      .offset(2)
                                      .length(4)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is("cdef")));
    }

    @Test
    void read_offsetBeyondEOF_returnsEmptyData() throws Exception {
        writeTempFile("short.txt", "abc");

        performRpc("read", ReadRequest.builder()
                                      .path("short.txt")
                                      .offset(100)
                                      .length(10)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is("")));
    }

    @Test
    void read_fileDoesNotExist_returnsError() throws Exception {
        performRpc("read", ReadRequest.builder()
                                      .path("missing.txt")
                                      .offset(0)
                                      .length(5)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("not found")));
    }

    @Test
    void read_whenPathIsDirectory_returnsError() throws Exception {
        createTempFolder("adir");

        performRpc("read", ReadRequest.builder()
                                      .path("adir")
                                      .offset(0)
                                      .length(5)
                                      .build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error.message", containsString("Invalid path")));
    }
}