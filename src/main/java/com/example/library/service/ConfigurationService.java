package com.example.library.service;

import com.example.library.dto.request.ConfigurationPageRequest;
import com.example.library.dto.request.ConfigurationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.ConfigurationResponse;
import com.example.library.dto.response.PageResponse;

public interface ConfigurationService {
    ApiResponse<ConfigurationResponse> create(ConfigurationRequest request);
    ApiResponse<ConfigurationResponse> update(ConfigurationRequest request);
    ApiResponse<Void> delete(Long id);
    ApiResponse<PageResponse<ConfigurationResponse>> search(ConfigurationPageRequest request);
}
