package com.example.library.service;

import com.example.library.dto.request.AuthorPageRequest;
import com.example.library.dto.request.AuthorRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthorResponse;
import com.example.library.dto.response.PageResponse;

public interface AuthorService {
    ApiResponse<AuthorResponse> create(AuthorRequest request);
    ApiResponse<AuthorResponse> update(AuthorRequest request);
    ApiResponse<Void> delete(Long id);
    ApiResponse<PageResponse<AuthorResponse>> search(AuthorPageRequest request);
}
