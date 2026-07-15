package com.example.library.service;

import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PublisherResponse;

public interface PublisherService {
    ApiResponse<PublisherResponse> create(PublisherRequest request);
}
