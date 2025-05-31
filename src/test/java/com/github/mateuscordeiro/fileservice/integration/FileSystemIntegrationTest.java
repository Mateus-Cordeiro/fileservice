package com.github.mateuscordeiro.fileservice.integration;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.nio.file.Files;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.AppendRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CopyRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CreateRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.DeleteRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ListChildrenRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.MoveRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ReadRequest;

import org.junit.jupiter.api.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class FileSystemIntegrationTest extends AbstractFileSystemIntegrationTest {
    @Test
    void createAppendRead_returnsExpectedData() throws Exception {
        String path = "compound.txt";
        String data = "JetBrains";

        performRpc("create", CreateRequest.builder().path(path).directory(false).build())
                .andExpect(status().isOk());

        performRpc("append", AppendRequest.builder().path(path).data(data).build())
                .andExpect(status().isOk());

        performRpc("read", ReadRequest.builder().path(path).offset(0).length(data.length()).build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is(data)));
    }

    @Test
    void createAndMoveFile_readFromNewLocationReturnsCorrectContent() throws Exception {
        String original = "move_me.txt";
        String moved = "sub/moved.txt";
        String content = "moving on";

        writeTempFile(original, content);

        performRpc("move", MoveRequest.builder().source(original).destination(moved).build())
                .andExpect(status().isOk());

        performRpc("read", ReadRequest.builder().path(moved).offset(0).length(content.length()).build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is(content)));
    }

    @Test
    void copyDirectory_thenListShowsCopiedFile() throws Exception {
        writeTempFile("src/inner/info.txt", "copy me");

        performRpc("copy", CopyRequest.builder().source("src").destination("dst").build())
                .andExpect(status().isOk());

        performRpc("listChildren", ListChildrenRequest.builder().path("dst/inner").build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[*].name", contains("info.txt")));
    }

    @Test
    void deleteTopLevelDirectory_removesAllNestedContent() throws Exception {
        writeTempFile("a/b/c/data.txt", "to delete");

        performRpc("delete", DeleteRequest.builder().path("a").build())
                .andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("a")));
    }

    @Test
    void createAppendMoveReadDelete_Succeeds() throws Exception {
        String original = "temp/note.txt";
        String moved = "notes/archive.txt";
        String content = "Content";

        performRpc("create", CreateRequest.builder().path(original).directory(false).build()).andExpect(
                status().isOk());

        performRpc("append", AppendRequest.builder().path(original).data(content).build()).andExpect(status().isOk());

        performRpc("move", MoveRequest.builder().source(original).destination(moved).build()).andExpect(
                status().isOk());

        performRpc("read", ReadRequest.builder().path(moved).offset(0).length(content.length()).build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is(content)));

        performRpc("delete", DeleteRequest.builder().path("notes").build()).andExpect(status().isOk());

        assertFalse(Files.exists(resolveTemp("notes")));
    }

    @Test
    void createFileThenCopyThenDeleteOriginal_succeeds() throws Exception {
        String content = "hello";
        writeTempFile("x.txt", content);

        performRpc("copy", CopyRequest.builder().source("x.txt").destination("y.txt").build())
                .andExpect(status().isOk());

        performRpc("delete", DeleteRequest.builder().path("x.txt").build())
                .andExpect(status().isOk());

        performRpc("read", ReadRequest.builder().path("y.txt").offset(0).length(content.length()).build())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.data", is(content)));

        assertFalse(Files.exists(resolveTemp("x.txt")));
    }
}