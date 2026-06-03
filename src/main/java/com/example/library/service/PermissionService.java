package com.example.library.service;

import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PermissionService {
    ApiResponse<PermissionResponse> create(PermissionRequest request);
    ApiResponse<PermissionResponse> update(PermissionRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<PermissionResponse>> search(PermissionPageRequest request);
    ApiResponse<ListResponse<SimpleResponse>> getAllStatusActive();
    void downloadTemplate(HttpServletResponse response) throws IOException;
    void export(PermissionPageRequest request, HttpServletResponse response) throws IOException;
    byte[] importFile(MultipartFile file);
}
