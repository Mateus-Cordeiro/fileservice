package com.github.mateuscordeiro.fileservice.rpc.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppendRequest {
    @NotBlank
    private String path;

    @NotNull
    private String data;
}