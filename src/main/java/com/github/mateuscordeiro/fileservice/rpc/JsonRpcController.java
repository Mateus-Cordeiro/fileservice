package com.github.mateuscordeiro.fileservice.rpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.mateuscordeiro.fileservice.exception.FileAlreadyExistsException;
import com.github.mateuscordeiro.fileservice.exception.FileNotFoundException;
import com.github.mateuscordeiro.fileservice.exception.InvalidPathException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.ErrorResolver.JsonError;
import com.googlecode.jsonrpc4j.JsonRpcServer;

@RestController
@RequestMapping("/filesystem")
public class JsonRpcController {
    private final JsonRpcServer jsonRpcServer;

    public JsonRpcController(FileSystemRpcHandler rpcHandler, ObjectMapper objectMapper) {
        this.jsonRpcServer = new JsonRpcServer(objectMapper, rpcHandler, FileSystemRpcHandler.class);

        this.jsonRpcServer.setErrorResolver((throwable, method, arguments) -> {
            if (throwable instanceof FileNotFoundException) {
                return new JsonError(-32001, throwable.getMessage(), null);
            } else if (throwable instanceof FileAlreadyExistsException) {
                return new JsonError(-32002, throwable.getMessage(), null);
            } else if (throwable instanceof InvalidPathException || throwable instanceof IllegalArgumentException) {
                return new JsonError(-32602, "Invalid parameters: " + throwable.getMessage(), null);
            }
            return JsonError.INTERNAL_ERROR;
        });
    }

    @PostMapping
    public void handleRpc(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (
                InputStream input = request.getInputStream();
                OutputStream output = response.getOutputStream()
        ) {
            jsonRpcServer.handleRequest(input, output);
        }
    }
}