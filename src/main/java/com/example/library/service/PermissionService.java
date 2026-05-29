package com.example.library.service;

import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.ListResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PermissionResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PermissionService {
    ApiResponse<PermissionResponse> create(PermissionRequest request);
    ApiResponse<PermissionResponse> update(PermissionRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<PermissionResponse>> search(PermissionPageRequest request);
    ApiResponse<ListResponse<PermissionResponse>> getAllStatusActive();
    void downloadTemplate(HttpServletResponse response) throws IOException;
    void export(PermissionPageRequest request, HttpServletResponse response) throws IOException;
    byte[] importFile(MultipartFile file);
}
