package com.example.library.controller;

import com.example.library.dto.request.AuthorPageRequest;
import com.example.library.dto.request.PublisherPageRequest;
import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthorResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PublisherResponse;
import com.example.library.service.PublisherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publisher")
@Tag(name = "Publisher Management")
public class PublisherController {
    private final PublisherService publisherService;

    @PostMapping
    public ResponseEntity<ApiResponse<PublisherResponse>> create(@RequestBody PublisherRequest request) {
        return ResponseEntity.ok(publisherService.create(request));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PublisherResponse>> update(@RequestBody PublisherRequest request) {
        return ResponseEntity.ok(publisherService.update(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(publisherService.delete(id));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<PublisherResponse>>> search(@RequestBody PublisherPageRequest request) {
        return ResponseEntity.ok(publisherService.search(request));
    }
}
