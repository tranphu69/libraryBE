package com.example.library.controller;

import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.RoleResponse;
import com.example.library.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;
    private static final String FILE_NAME = "Template_role.xlsx";

    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> create(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.create(request));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<RoleResponse>> update(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.update(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(roleService.delete(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> search(@RequestBody RolePageRequest request) {
        return ResponseEntity.ok(roleService.search(request));
    }
}
