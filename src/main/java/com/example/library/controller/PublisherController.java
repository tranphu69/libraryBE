package com.example.library.controller;

import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PublisherResponse;
import com.example.library.service.PublisherService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
