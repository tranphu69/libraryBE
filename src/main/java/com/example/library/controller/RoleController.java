package com.example.library.controller;

import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.RoleRequest;
import com.example.library.dto.response.*;
import com.example.library.service.RoleService;
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
@RequestMapping("/api/roles")
@Tag(name = "Role Management")
public class RoleController {
    private final RoleService roleService;
    private static final String FILE_NAME = "Template_role.xlsx";

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> create(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponse>> update(@RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(roleService.delete(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_SEARCH')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> search(@RequestBody RolePageRequest request) {
        return ResponseEntity.ok(roleService.search(request));
    }

    @GetMapping("/all-status-active")
    @PreAuthorize("hasAuthority('ROLE_SEARCH')")
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
    @PreAuthorize("hasAuthority('ROLE_SEARCH')")
    public void export(RolePageRequest request, HttpServletResponse response) throws IOException {
        roleService.export(request, response);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<Object> importFile(@RequestParam MultipartFile file) {
        byte[] errorFile = roleService.importFile(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=IMPORT_VAI_TRO_KET_QUA.xlsx")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(errorFile);
    }
}
