package com.github.mateuscordeiro.fileservice.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.github.mateuscordeiro.fileservice.config.RootPathProperties;
import com.github.mateuscordeiro.fileservice.testutil.JsonRpcRequestFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class AbstractFileSystemIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RootPathProperties rootPathProperties;

    @TempDir
    protected Path tempDir;

    @BeforeEach
    void setup() {
        rootPathProperties.setRoot(tempDir);
    }

    protected ResultActions performRpc(String method, Object dto) throws Exception {
        return mockMvc.perform(post("/filesystem")
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .content(JsonRpcRequestFactory.createRequest(method, dto)));
    }

    protected Path resolveTemp(String relativePath) {
        return tempDir.resolve(relativePath);
    }

    protected void writeTempFile(String relativePath, String content) throws IOException {
        Path file = resolveTemp(relativePath);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    protected void createTempFolder(String relativePath) throws IOException {
        Files.createDirectories(resolveTemp(relativePath));
    }
}