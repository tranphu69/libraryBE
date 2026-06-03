package com.example.library.controller;

import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.*;
import com.example.library.service.RoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

    @GetMapping("/all-status-active")
    public ResponseEntity<ApiResponse<ListResponse<SimpleResponse>>> statusActive() {
        return ResponseEntity.ok(roleService.getAllStatusActive());
    }

    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + FILE_NAME);
        roleService.downloadTemplate(response);
    }

    @PostMapping("/export")
    public void export(RolePageRequest request, HttpServletResponse response) throws IOException {
        roleService.export(request, response);
    }
}
