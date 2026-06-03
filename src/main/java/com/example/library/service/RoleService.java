package com.example.library.service;

import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface RoleService {
    ApiResponse<RoleResponse> create(RoleRequest request);
    ApiResponse<RoleResponse> update(RoleRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<RoleResponse>> search(RolePageRequest request);
    ApiResponse<ListResponse<SimpleResponse>> getAllStatusActive();
    void downloadTemplate(HttpServletResponse response) throws IOException;
    void export(RolePageRequest request, HttpServletResponse response) throws IOException;
}
