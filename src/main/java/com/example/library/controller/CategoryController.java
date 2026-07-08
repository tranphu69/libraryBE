package com.example.library.controller;

import com.example.library.dto.request.CategoryRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.CategoryResponse;
import com.example.library.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categorys")
@Tag(name = "Category Management")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(request));
    }
}
