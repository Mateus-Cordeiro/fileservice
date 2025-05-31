package com.github.mateuscordeiro.fileservice.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonRpcRequestFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String createRequest(String method, Object paramDto) {
        try {
            String jsonParam = mapper.writeValueAsString(paramDto);
            return String.format("""
                    {
                      "jsonrpc": "2.0",
                      "method": "%s",
                      "params": [%s],
                      "id": %d
                    }
                    """, method, jsonParam, 1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize request", e);
        }
    }
}