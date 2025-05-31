package com.github.mateuscordeiro.fileservice.rpc.dto.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadRequest {
    @NotBlank
    private String path;

    @Min(0)
    private int offset;

    @Min(1)
    private int length;
}