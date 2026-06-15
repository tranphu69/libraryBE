package com.example.library.controller;

import com.example.library.dto.request.UserPageRequest;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private static final String FILE_NAME = "Template_user.xlsx";

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.create(request));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> update(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        return ResponseEntity.ok(userService.delete(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> search(@RequestBody UserPageRequest request) {
        return ResponseEntity.ok(userService.search(request));
    }

    @GetMapping("/download-template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + FILE_NAME);
        userService.downloadTemplate(response);
    }

    @PostMapping("/export")
    public void export(UserPageRequest request, HttpServletResponse response) throws IOException {
        userService.export(request, response);
    }

    @PostMapping("/import")
    public ResponseEntity<Object> importFile(@RequestParam MultipartFile file) {
        byte[] errorFile = userService.importFile(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=IMPORT_VAI_TRO_KET_QUA.xlsx")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(errorFile);
    }
}
