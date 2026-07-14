package com.example.library.service.service_impl;

import com.example.library.aspect.Auditable;
import com.example.library.constant.AppConstant;
import com.example.library.constant.CategoryConstant;
import com.example.library.domain.Category;
import com.example.library.dto.request.CategoryPageRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, CategoryConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), CategoryConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, CategoryConstant.NAME, CategoryConstant.NAME_LENGTH);
        }
        if(DataUtils.maxLengthNotEmpty(request.getDescription(), CategoryConstant.MAX_LENGTH_DESCRIPTION)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, CategoryConstant.DESCRIPTION, CategoryConstant.DESCRIPTION_LENGTH);
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
        Category parent = categoryRepository.findByIdAndIsDeletedNot(parentId, CategoryConstant.DELETE)
                .filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, CategoryConstant.CATEGORY));
        int parentLevel = getLevel(parent);
        return parentLevel + 1 <= MAX_LEVEL;
    }

    @Override
    @Transactional
    @Auditable(action = "CREATE_CATEGORY", targetType = "CATEGORY", targetId = "#request.id")
    public ApiResponse<CategoryResponse> create(CategoryRequest request) {
        log.info("Creating new category with code: {}", request.getCode());
        validate(request);
        if(categoryRepository.existsActiveCode(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, CategoryConstant.CODE);
        }
        if(request.getParentId() != null && !categoryRepository.existsByIdAndIsDeletedNot(request.getParentId(), CategoryConstant.DELETE)) {
            throw new BusinessException(ErrorCode.NOT_EXIST, CategoryConstant.PARENT_CATEGORY);
        }else if (!canAddChild(request.getParentId())) {
            throw new BusinessException(ErrorCode.MAX_LEVEL, CategoryConstant.CATEGORY, MAX_LEVEL);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Category category = Category.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .parentId(request.getParentId())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .createdBy(authentication.getName())
                .updatedAt(LocalDateTime.now())
                .build();
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        CategoryResponse response = categoryMapper.toCategoryResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "UPDATE_CATEGORY", targetType = "CATEGORY", targetId = "#request.id")
    public ApiResponse<CategoryResponse> update(CategoryRequest request) {
        log.info("Updating category with id: {}", request.getId());
        Category category = categoryRepository.findByIdAndIsDeletedNot(request.getId(), CategoryConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, CategoryConstant.CATEGORY));
        validate(request);
        if(categoryRepository.existsActiveCodeAndNotId(request.getCode().trim().toUpperCase(), request.getId())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, CategoryConstant.CODE);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        category.setCode(request.getCode().trim().toUpperCase());
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription().trim());
        category.setUpdatedBy(authentication.getName());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", category.getId());
        CategoryResponse response = categoryMapper.toCategoryResponse(category);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    @Transactional
    @Auditable(action = "DELETE_CATEGORY", targetType = "CATEGORY", targetId = "#id")
    public ApiResponse<Void> delete(Long id) {
        log.info("Deleting category with id: {}", id);
        Category category = categoryRepository.findByIdAndIsDeletedNot(id, CategoryConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, CategoryConstant.CATEGORY));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        category.setIsDeleted(CategoryConstant.DELETE);
        category.setUpdatedBy(authentication.getName());
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<List<CategoryResponse>> search(CategoryPageRequest request) {
        List<Category> result;
        if (DataUtils.isBlank(request.getCode()) && DataUtils.isBlank(request.getName())) {
            result = categoryRepository.findAllByIsDeletedNot(CategoryConstant.DELETE);
        } else {
            List<Category> matched = categoryRepository.search(request.getCode(), request.getName());
            if (matched.isEmpty()) {
                return ResponseUtils.success(Collections.emptyList(), AppConstant.SUCCESS);
            }
            List<Long> matchedIds = matched.stream().map(Category::getId).toList();
            result = categoryRepository.findAllWithAncestors(matchedIds);
        }
        List<CategoryResponse> response = result.stream()
                .map(categoryMapper::toCategoryResponse)
                .toList();
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }
}
