package com.github.mateuscordeiro.fileservice.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "fileservice")
@Component
public class RootPathProperties {
    private Path root;
}
