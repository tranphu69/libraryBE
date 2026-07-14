package com.example.library.controller;

import com.example.library.dto.request.AuthorPageRequest;
import com.example.library.dto.request.AuthorRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthorResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.service.AuthorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/author")
@Tag(name = "Author Management")
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping
    @PreAuthorize("hasAuthority('AUTHOR_CREATE')")
    public ResponseEntity<ApiResponse<AuthorResponse>> create(@RequestBody AuthorRequest request) {
        return ResponseEntity.ok(authorService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('AUTHOR_UPDATE')")
    public ResponseEntity<ApiResponse<AuthorResponse>> update(@RequestBody AuthorRequest request) {
        return ResponseEntity.ok(authorService.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AUTHOR_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(authorService.delete(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('AUTHOR_SEARCH')")
    public ResponseEntity<ApiResponse<PageResponse<AuthorResponse>>> search(@RequestBody AuthorPageRequest request) {
        return ResponseEntity.ok(authorService.search(request));
    }
}
