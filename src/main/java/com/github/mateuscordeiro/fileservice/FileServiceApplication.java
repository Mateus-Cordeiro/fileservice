package com.github.mateuscordeiro.fileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.github.mateuscordeiro.fileservice.config.RootPathProperties;

@SpringBootApplication
@EnableConfigurationProperties(RootPathProperties.class)
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }

}
