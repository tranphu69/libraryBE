package com.example.library.service.service_impl;

import com.example.library.aspect.Auditable;
import com.example.library.constant.AppConstant;
import com.example.library.constant.AuthorConstant;
import com.example.library.constant.PublisherConstant;
import com.example.library.domain.Publisher;
import com.example.library.dto.request.PublisherPageRequest;
import com.example.library.dto.request.PublisherRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PublisherResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.PublisherMapper;
import com.example.library.repository.PublisherRepository;
import com.example.library.service.PublisherService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class PublisherServiceImpl implements PublisherService {
    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    private void validate(PublisherRequest request) {
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, PublisherConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), PublisherConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PublisherConstant.NAME, PublisherConstant.NAME_LENGTH);
        }
        if(!DataUtils.isBlank(request.getAddress()) && DataUtils.maxLength(request.getName(), PublisherConstant.MAX_LENGTH_ADDRESS)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, PublisherConstant.ADDRESS, PublisherConstant.ADDRESS_LENGTH);
        }
        if(!DataUtils.isBlank(request.getEmail()) && !DataUtils.isValidEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.NOT_VALID, PublisherConstant.EMAIL);
        }
        if(!DataUtils.isBlank(request.getPhone()) && DataUtils.isValidPhone(request.getPhone())) {
            throw new BusinessException(ErrorCode.NOT_VALID, PublisherConstant.PHONE);
        }
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE_PUBLISHER", targetType = "PUBLISHER", targetId = "#request.id")
    public ApiResponse<PublisherResponse> create(PublisherRequest request) {
        log.info("Creating new publisher with code: {}", request.getName());
        validate(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Publisher publisher = Publisher.builder()
                .name(request.getName().trim())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .createdBy(authentication.getName())
                .updatedAt(LocalDateTime.now())
                .build();
        Publisher saved = publisherRepository.save(publisher);
        log.info("Publisher created successfully with id: {}", saved.getId());
        PublisherResponse response = publisherMapper.toPublisherResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_PUBLISHER", targetType = "PUBLISHER", targetId = "#request.id")
    public ApiResponse<PublisherResponse> update(PublisherRequest request) {
        log.info("Updating publisher with id: {}", request.getId());
        Publisher publisher = publisherRepository.findByIdAndIsDeletedNot(request.getId(), AuthorConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PublisherConstant.PUBLISHER));
        validate(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        publisher.setName(request.getName().trim());
        publisher.setAddress(request.getAddress());
        publisher.setEmail(request.getEmail());
        publisher.setPhone(request.getPhone());
        publisher.setUpdatedBy(authentication.getName());
        publisher.setUpdatedAt(LocalDateTime.now());
        publisherRepository.save(publisher);
        log.info("Publisher updated successfully with id: {}", publisher.getId());
        PublisherResponse response = publisherMapper.toPublisherResponse(publisher);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE_PUBLISHER", targetType = "PUBLISHER", targetId = "#id")
    public ApiResponse<Void> delete(Long id) {
        log.info("Deleting publisher with id: {}", id);
        Publisher publisher = publisherRepository.findByIdAndIsDeletedNot(id, AuthorConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, PublisherConstant.PUBLISHER));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        publisher.setIsDeleted(PublisherConstant.DELETE);
        publisher.setUpdatedBy(authentication.getName());
        publisher.setUpdatedAt(LocalDateTime.now());
        publisherRepository.save(publisher);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(PublisherPageRequest request) {
        if(DataUtils.isNull(request.getPage())) {
            request.setPage(0);
        }
        if(DataUtils.isNull(request.getSize())) {
            request.setSize(10);
        }
        if(DataUtils.isBlank(request.getSortBy())) {
            request.setSortBy(AuthorConstant.UPDATED_AT);
        }
        if(DataUtils.isBlank(request.getSortDir())) {
            request.setSortDir(AuthorConstant.DESC);
        }
    }

    @Override
    public ApiResponse<PageResponse<PublisherResponse>> search(PublisherPageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Publisher> page = publisherRepository.search(request.getName(), request.getAddress(), request.getEmail(), request.getPhone(), pageable);
        return null;
    }
}
