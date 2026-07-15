package com.example.library.service;

import com.example.library.dto.request.AuthorPageRequest;
import com.example.library.dto.request.PublisherPageRequest;
import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthorResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PublisherResponse;

public interface PublisherService {
    ApiResponse<PublisherResponse> create(PublisherRequest request);
    ApiResponse<PublisherResponse> update(PublisherRequest request);
    ApiResponse<Void> delete(Long id);
    ApiResponse<PageResponse<PublisherResponse>> search(PublisherPageRequest request);
}
