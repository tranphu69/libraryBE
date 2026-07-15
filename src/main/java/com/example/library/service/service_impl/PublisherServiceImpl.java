package com.example.library.service.service_impl;

import com.example.library.constant.PublisherConstant;
import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PublisherResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.repository.PublisherRepository;
import com.example.library.service.PublisherService;
import com.example.library.util.DataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;

    private void validate(PublisherRequest request) {
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PublisherConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), PublisherConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PublisherConstant.NAME, PublisherConstant.NAME_LENGTH);
        }
    }

    @Override
    public ApiResponse<PublisherResponse> create(PublisherRequest request) {
        log.info("Creating new publisher with code: {}", request.getName());
        validate(request);
        return null;
    }
}
