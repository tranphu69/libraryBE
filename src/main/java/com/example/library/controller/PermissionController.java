package com.example.library.controller;

import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.ListResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PermissionResponse;
import com.example.library.service.PermissionService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
public class PermissionController {
    private final PermissionService permissionService;
    private static final String FILE_NAME  = "Template_permission.xlsx";

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.create(request));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> update(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.delete(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> search(@RequestBody PermissionPageRequest request) {
        return ResponseEntity.ok(permissionService.search(request));
    }

    @GetMapping("/all-status-active")
    public ResponseEntity<ApiResponse<ListResponse<PermissionResponse>>> statusActive() {
        return ResponseEntity.ok(permissionService.getAllStatusActive());
    }

    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + FILE_NAME);
        permissionService.downloadTemplate(response);
    }
}
