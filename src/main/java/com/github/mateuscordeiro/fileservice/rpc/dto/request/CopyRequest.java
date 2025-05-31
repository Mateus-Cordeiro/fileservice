package com.github.mateuscordeiro.fileservice.rpc.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyRequest {
    @NotBlank
    private String source;

    @NotBlank
    private String destination;
}