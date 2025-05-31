package com.github.mateuscordeiro.fileservice.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {
    private String name;
    private String path;
    private long size;
    private boolean directory;
}