package com.example.library.controller;

import com.example.library.dto.request.PermissionPageRequest;
import com.example.library.dto.request.PermissionRequest;
import com.example.library.dto.response.*;
import com.example.library.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/permissions")
@Tag(name = "Permission Management")
public class PermissionController {
    private final PermissionService permissionService;
    private static final String FILE_NAME = "Template_permission.xlsx";

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> create(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PERMISSION_UPDATE')")
    public ResponseEntity<ApiResponse<PermissionResponse>> update(@RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISSION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.delete(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('PERMISSION_SEARCH')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> search(@RequestBody PermissionPageRequest request) {
        return ResponseEntity.ok(permissionService.search(request));
    }

    @GetMapping("/all-status-active")
    @PreAuthorize("hasAuthority('PERMISSION_SEARCH')")
    public ResponseEntity<ApiResponse<ListResponse<SimpleResponse>>> statusActive() {
        return ResponseEntity.ok(permissionService.getAllStatusActive());
    }

    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + FILE_NAME);
        permissionService.downloadTemplate(response);
    }

    @PostMapping("/export")
    @PreAuthorize("hasAuthority('PERMISSION_SEARCH')")
    public void export(PermissionPageRequest request, HttpServletResponse response) throws IOException {
        permissionService.export(request, response);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('PERMISSION_CREATE')")
    public ResponseEntity<Object> importFile(@RequestParam MultipartFile file) {
        byte[] errorFile = permissionService.importFile(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=IMPORT_QUYEN_KET_QUA.xlsx")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(errorFile);
    }
}
