package com.example.library.service;

import com.example.library.dto.request.UserPageRequest;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.UserResponse;

public interface UserService {
    ApiResponse<UserResponse> create(UserRequest request);
    ApiResponse<UserResponse> update(UserRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<UserResponse>> search(UserPageRequest request);
}
