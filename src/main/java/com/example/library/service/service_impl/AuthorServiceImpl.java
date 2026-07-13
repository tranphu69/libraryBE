package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.AuthorConstant;
import com.example.library.domain.Author;
import com.example.library.dto.request.AuthorPageRequest;
import com.example.library.dto.request.AuthorRequest;
import com.example.library.dto.response.*;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.AuthorMapper;
import com.example.library.repository.AuthorRepository;
import com.example.library.service.AuthorService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private static final String REGEX = "\\w+";

    private void validate(AuthorRequest request) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, AuthorConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), AuthorConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, AuthorConstant.CODE, AuthorConstant.CODE_LENGTH);
        } else if(!request.getCode().matches(REGEX)) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, AuthorConstant.CODE);
        } else if(authorRepository.existsActiveCode(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, AuthorConstant.CODE);
        }
        if(DataUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, AuthorConstant.NAME);
        } else if(DataUtils.maxLength(request.getName(), AuthorConstant.MAX_LENGTH_NAME)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, AuthorConstant.NAME, AuthorConstant.NAME_LENGTH);
        }
        if(!DataUtils.isBlank(request.getDateBirth()) && DataUtils.isValidDate(request.getDateBirth())) {
            throw new BusinessException(ErrorCode.NOT_VALID, AuthorConstant.DATE_BIRTH);
        }
        if(!DataUtils.isBlank(request.getDateDeath()) && DataUtils.isValidDate(request.getDateDeath())) {
            throw new BusinessException(ErrorCode.NOT_VALID, AuthorConstant.DATE_DEATH);
        }
    }

    @Override
    @Transactional
    public ApiResponse<AuthorResponse> create(AuthorRequest request) {
        log.info("Creating new author with code: {}", request.getCode());
        validate(request);
        Author author = Author.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .dateOfBirth(DataUtils.parseDate(request.getDateBirth()))
                .dateOfDeath(DataUtils.parseDate(request.getDateDeath()))
                .nationality(request.getNationality())
                .biography(request.getBiography())
                .imageUrl(request.getImageURL())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Author saved = authorRepository.save(author);
        log.info("Author created successfully with id: {}", saved.getId());
        AuthorResponse response = authorMapper.toAuthorResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<AuthorResponse> update(AuthorRequest request) {
        log.info("Updating author with id: {}", request.getId());
        Author author = authorRepository.findByIdAndIsDeletedNot(request.getId(), AuthorConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, AuthorConstant.AUTHOR));
        validate(request);
        author.setCode(request.getCode().trim().toUpperCase());
        author.setName(request.getName().trim());
        author.setDateOfBirth(DataUtils.parseDate(request.getDateBirth()));
        author.setDateOfDeath(DataUtils.parseDate(request.getDateDeath()));
        author.setNationality(request.getNationality());
        author.setBiography(request.getBiography());
        author.setImageUrl(request.getImageURL());
        author.setUpdatedAt(LocalDateTime.now());
        authorRepository.save(author);
        log.info("Author updated successfully with id: {}", author.getId());
        AuthorResponse response = authorMapper.toAuthorResponse(author);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<Void> delete(Long id) {
        log.info("Deleting author with id: {}", id);
        Author author = authorRepository.findByIdAndIsDeletedNot(id, AuthorConstant.DELETE)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, AuthorConstant.AUTHOR));
        author.setIsDeleted(AuthorConstant.DELETE);
        author.setUpdatedAt(LocalDateTime.now());
        authorRepository.save(author);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(AuthorPageRequest request) {
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
    public ApiResponse<PageResponse<AuthorResponse>> search(AuthorPageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<Author> page = authorRepository.search(request.getCode(), request.getName(), request.getNationality(), request.getDateBirth(), request.getDateDeath(), pageable);
        List<AuthorResponse> content = page.getContent().stream()
                .map(authorMapper::toAuthorResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<AuthorResponse> result = PageResponse.<AuthorResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }
}
