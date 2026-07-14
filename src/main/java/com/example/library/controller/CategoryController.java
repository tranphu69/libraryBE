package com.example.library.controller;

import com.example.library.dto.request.CategoryPageRequest;
import com.example.library.dto.request.CategoryRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.CategoryResponse;
import com.example.library.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
@Tag(name = "Category Management")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.delete(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('CATEGORY_SEARCH')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> search(@RequestBody CategoryPageRequest request) {
        return ResponseEntity.ok(categoryService.search(request));
    }
}
