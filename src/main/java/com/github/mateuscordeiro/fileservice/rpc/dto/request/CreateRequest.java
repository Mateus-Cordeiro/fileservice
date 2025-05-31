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
public class CreateRequest {
    @NotBlank
    private String path;

    private boolean directory;
}