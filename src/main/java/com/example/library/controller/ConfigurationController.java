package com.example.library.controller;

import com.example.library.dto.request.ConfigurationPageRequest;
import com.example.library.dto.request.ConfigurationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.ConfigurationResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.service.ConfigurationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/configuration")
@Tag(name = "Configuration Management")
public class ConfigurationController {
    private final ConfigurationService configurationService;

    @PostMapping
    @PreAuthorize("hasAuthority('CONFIGURATION_CREATE')")
    public ResponseEntity<ApiResponse<ConfigurationResponse>> create(@RequestBody ConfigurationRequest request) {
        return ResponseEntity.ok(configurationService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('CONFIGURATION_UPDATE')")
    public ResponseEntity<ApiResponse<ConfigurationResponse>> update(@RequestBody ConfigurationRequest request) {
        return ResponseEntity.ok(configurationService.update(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(configurationService.delete(id));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('CONFIGURATION_SEARCH')")
    public ResponseEntity<ApiResponse<PageResponse<ConfigurationResponse>>> search(@RequestBody ConfigurationPageRequest request) {
        return ResponseEntity.ok(configurationService.search(request));
    }
}
