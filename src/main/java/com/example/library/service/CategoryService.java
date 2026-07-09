package com.example.library.service;

import com.example.library.dto.request.CategoryPageRequest;
import com.example.library.dto.request.CategoryRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    ApiResponse<CategoryResponse> create(CategoryRequest request);
    ApiResponse<CategoryResponse> update(CategoryRequest request);
    ApiResponse<Void> delete(Long id);
    ApiResponse<List<CategoryResponse>> search(CategoryPageRequest request);
}
