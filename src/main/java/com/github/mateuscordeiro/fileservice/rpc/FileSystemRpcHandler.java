package com.github.mateuscordeiro.fileservice.rpc;

import java.util.List;

import org.springframework.stereotype.Component;
import com.github.mateuscordeiro.fileservice.rpc.dto.FileInfo;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.AppendRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CopyRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.CreateRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.DeleteRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.GetFileInfoRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ListChildrenRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.MoveRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.request.ReadRequest;
import com.github.mateuscordeiro.fileservice.rpc.dto.response.ReadResponse;
import com.github.mateuscordeiro.fileservice.service.FileSystemService;
import com.github.mateuscordeiro.fileservice.validation.ValidationUtils;

@Component
public class FileSystemRpcHandler {
    private final FileSystemService service;

    public FileSystemRpcHandler(FileSystemService service) {
        this.service = service;
    }

    public FileInfo getFileInfo(GetFileInfoRequest path) {
        ValidationUtils.requireNonBlank(path.getPath(), "path");

        return service.getFileInfo(path.getPath());
    }

    public List<FileInfo> listChildren(ListChildrenRequest path) {
        ValidationUtils.requireNonBlank(path.getPath(), "path");

        return service.listChildren(path.getPath());
    }

    public void create(CreateRequest request) {
        ValidationUtils.requireNonBlank(request.getPath(), "path");

        service.create(request.getPath(), request.isDirectory());
    }

    public void delete(DeleteRequest request) {
        ValidationUtils.requireNonBlank(request.getPath(), "path");

        service.delete(request.getPath());
    }

    public void move(MoveRequest request) {
        ValidationUtils.requireNonBlank(request.getSource(), "source");
        ValidationUtils.requireNonBlank(request.getDestination(), "destination");

        service.move(request.getSource(), request.getDestination());
    }

    public void copy(CopyRequest request) {
        ValidationUtils.requireNonBlank(request.getSource(), "source");
        ValidationUtils.requireNonBlank(request.getDestination(), "destination");

        service.copy(request.getSource(), request.getDestination());
    }

    public void append(AppendRequest request) {
        ValidationUtils.requireNonBlank(request.getPath(), "path");
        ValidationUtils.requireNonNull(request.getData(), "data");

        service.append(request.getPath(), request.getData());
    }

    public ReadResponse read(ReadRequest request) {
        ValidationUtils.requireNonBlank(request.getPath(), "path");
        ValidationUtils.requireNonNegative(request.getOffset(), "offset");
        ValidationUtils.requirePositive(request.getLength(), "length");

        return new ReadResponse(service.read(request.getPath(), request.getOffset(), request.getLength()));
    }
}