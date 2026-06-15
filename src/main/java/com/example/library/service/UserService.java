package com.example.library.service;

import com.example.library.dto.request.UserPageRequest;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    ApiResponse<UserResponse> create(UserRequest request);
    ApiResponse<UserResponse> update(UserRequest request);
    ApiResponse<Void> delete(String id);
    ApiResponse<PageResponse<UserResponse>> search(UserPageRequest request);
    void downloadTemplate(HttpServletResponse response) throws IOException;
    void export(UserPageRequest request, HttpServletResponse response) throws IOException;
    byte[] importFile(MultipartFile file);
}
