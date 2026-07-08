package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.CategoryConstant;
import com.example.library.domain.Category;
import com.example.library.dto.request.CategoryRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.CategoryResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.CategoryMapper;
import com.example.library.repository.CategoryRepository;
import com.example.library.service.CategoryService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private static final String REGEX = "\\w+";
    private static final int MAX_LEVEL = 3;

    private void validate(CategoryRequest request) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, CategoryConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), CategoryConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, CategoryConstant.CODE, CategoryConstant.CODE_LENGTH);
        } else if(!request.getCode().matches(REGEX)) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, CategoryConstant.CODE);
        } else if(categoryRepository.existsActiveCode(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, CategoryConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, CategoryConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), CategoryConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, CategoryConstant.NAME, CategoryConstant.NAME_LENGTH);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), CategoryConstant.MAX_LENGTH_DESCRIPTION)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, CategoryConstant.DESCRIPTION, CategoryConstant.DESCRIPTION_LENGTH);
        }
        if(request.getParentId() != null && !categoryRepository.existsByIdAndIsDeletedNot(request.getParentId(), true)) {
            throw new BusinessException(ErrorCode.NOT_EXIST, CategoryConstant.PARENT_CATEGORY);
        }else if (!canAddChild(request.getParentId())) {
            throw new BusinessException(ErrorCode.MAX_LEVEL, CategoryConstant.CATEGORY, MAX_LEVEL);
        }
    }

    private int getLevel(Category category) {
        int level = 1;
        Category current = category;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    private boolean canAddChild(Long parentId) {
        if (parentId == null) {
            return true;
        }
        Category parent = categoryRepository.findById(parentId)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Category cha hoặc đã bị xóa"));
        int parentLevel = getLevel(parent);
        return parentLevel + 1 <= MAX_LEVEL;
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> create(CategoryRequest request) {
        log.info("Creating new category with code: {}", request.getCode());
        validate(request);
        Category category = Category.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .parentId(request.getParentId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        CategoryResponse response = categoryMapper.toCategoryResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    public ApiResponse<CategoryResponse> update(CategoryRequest request) {
        log.info("Updating category with id: {}", request.getId());
        Category category = categoryRepository.findByIdAndStatusNot(request.getId(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, CategoryConstant.CATEGORY));
        validate(request);
        return null;
    }
}
