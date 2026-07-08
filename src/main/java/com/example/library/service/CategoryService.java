package com.example.library.service;

import com.example.library.dto.request.CategoryRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.CategoryResponse;

public interface CategoryService {
    ApiResponse<CategoryResponse> create(CategoryRequest request);
    ApiResponse<CategoryResponse> update(CategoryRequest request);
}
