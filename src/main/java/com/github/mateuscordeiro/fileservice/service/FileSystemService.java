package com.github.mateuscordeiro.fileservice.service;

import java.util.List;

import com.github.mateuscordeiro.fileservice.rpc.dto.FileInfo;

public interface FileSystemService {
    FileInfo getFileInfo(String path);

    List<FileInfo> listChildren(String path);

    void create(String path, boolean isDirectory);

    void delete(String path);

    void move(String sourcePath, String destinationPath);

    void copy(String sourcePath, String destinationPath);

    void append(String path, String data);

    String read(String path, int offset, int length);
}
