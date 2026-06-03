package com.example.library.service;

import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.RoleResponse;

public interface RoleService {
    ApiResponse<RoleResponse> create(RoleRequest request);
    ApiResponse<RoleResponse> update(RoleRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<RoleResponse>> search(RolePageRequest request);
}
