package com.github.mateuscordeiro.fileservice.rpc.dto.request;

import lombok.Data;

@Data
public class GetFileInfoRequest {
    private String path;
}
