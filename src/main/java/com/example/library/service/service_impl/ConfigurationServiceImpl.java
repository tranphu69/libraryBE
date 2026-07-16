package com.example.library.service.service_impl;

import com.example.library.aspect.Auditable;
import com.example.library.constant.AppConstant;
import com.example.library.constant.AuthorConstant;
import com.example.library.constant.ConfigurationConstant;
import com.example.library.domain.Configuration;
import com.example.library.dto.request.ConfigurationPageRequest;
import com.example.library.dto.request.ConfigurationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.ConfigurationResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PaginationMeta;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.ConfigurationMapper;
import com.example.library.repository.ConfigurationRepository;
import com.example.library.service.ConfigurationService;
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
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConfigurationServiceImpl implements ConfigurationService {
    private final ConfigurationRepository configurationRepository;
    private final ConfigurationMapper configurationMapper;
    private static final String REGEX = "\\w+";

    private void validate(ConfigurationRequest request) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, ConfigurationConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), ConfigurationConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, ConfigurationConstant.CODE, ConfigurationConstant.CODE_LENGTH);
        } else if(!request.getCode().matches(REGEX)) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, ConfigurationConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, ConfigurationConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), ConfigurationConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, ConfigurationConstant.NAME, ConfigurationConstant.NAME_LENGTH);
        }
        if(DataUtils.isNull(request.getValue())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, ConfigurationConstant.VALUE);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), ConfigurationConstant.MAX_LENGTH_DESCRIPTION)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, ConfigurationConstant.DESCRIPTION, ConfigurationConstant.DESCRIPTION_LENGTH);
        }
        if(DataUtils.isBlank(request.getType())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, ConfigurationConstant.TYPE);
        } else if(DataUtils.maxLength(request.getType(), ConfigurationConstant.MAX_LENGTH_TYPE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, ConfigurationConstant.TYPE, ConfigurationConstant.TYPE_LENGTH);
        }
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE_CONFIGURATION", targetType = "CONFIGURATION", targetId = "#request.id")
    public ApiResponse<ConfigurationResponse> create(ConfigurationRequest request) {
        log.info("Creating new configuration with code: {}", request.getCode());
        validate(request);
        if(configurationRepository.existsActiveCode(request.getCode().trim().toUpperCase(), request.getType().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE_TYPE, ConfigurationConstant.CODE, ConfigurationConstant.TYPE);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Configuration configuration = Configuration.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .type(request.getType().trim().toUpperCase())
                .value(request.getValue())
                .description(request.getDescription())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .createdBy(authentication.getName())
                .updatedAt(LocalDateTime.now())
                .build();
        Configuration saved = configurationRepository.save(configuration);
        log.info("Configuration created successfully with id: {}", saved.getId());
        ConfigurationResponse response = configurationMapper.toConfigurationResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_CONFIGURATION", targetType = "CONFIGURATION", targetId = "#request.id")
    public ApiResponse<ConfigurationResponse> update(ConfigurationRequest request) {
        log.info("Updating configuration with id: {}", request.getId());
        Configuration configuration = configurationRepository.findByIdAndIsDeletedNot(request.getId(), ConfigurationConstant.DELETE)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, ConfigurationConstant.CONFIGURATION));
        validate(request);
        if(configurationRepository.existsActiveCodeAndNotId(request.getCode().trim().toUpperCase(), request.getType().trim().toUpperCase(), request.getId())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE_TYPE, ConfigurationConstant.CODE, ConfigurationConstant.TYPE);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        configuration.setCode(request.getCode().trim().toUpperCase());
        configuration.setName(request.getName().trim());
        configuration.setType(request.getType().trim().toUpperCase());
        configuration.setValue(request.getValue());
        configuration.setDescription(request.getDescription());
        configuration.setUpdatedBy(authentication.getName());
        configuration.setUpdatedAt(LocalDateTime.now());
        configurationRepository.save(configuration);
        log.info("Configuration updated successfully with id: {}", configuration.getId());
        ConfigurationResponse response = configurationMapper.toConfigurationResponse(configuration);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE_CONFIGURATION", targetType = "CONFIGURATION", targetId = "#id")
    public ApiResponse<Void> delete(Long id) {
        log.info("Deleting configuration with id: {}", id);
        Configuration configuration = configurationRepository.findByIdAndIsDeletedNot(id, ConfigurationConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, ConfigurationConstant.CONFIGURATION));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        configuration.setIsDeleted(AuthorConstant.DELETE);
        configuration.setUpdatedBy(authentication.getName());
        configuration.setUpdatedAt(LocalDateTime.now());
        configurationRepository.save(configuration);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(ConfigurationPageRequest request) {
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
    public ApiResponse<PageResponse<ConfigurationResponse>> search(ConfigurationPageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Configuration> page = configurationRepository.search(request.getCode(), request.getName(), request.getDescription(), request.getType(), pageable);
        List<ConfigurationResponse> content = page.getContent().stream()
                .map(configurationMapper::toConfigurationResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<ConfigurationResponse> result = PageResponse.<ConfigurationResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }
}
