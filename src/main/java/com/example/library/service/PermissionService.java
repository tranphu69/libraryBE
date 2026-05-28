package com.example.library.service;

import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PermissionResponse;

public interface PermissionService {
    ApiResponse<PermissionResponse> create(PermissionRequest request);
    ApiResponse<PermissionResponse> update(PermissionRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<PermissionResponse>> search(PermissionPageRequest request);
}
