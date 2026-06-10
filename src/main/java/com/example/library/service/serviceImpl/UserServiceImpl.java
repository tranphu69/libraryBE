package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.PermissionConstant;
import com.example.library.constant.UserConstant;
import com.example.library.domain.Role;
import com.example.library.domain.User;
import com.example.library.dto.request.RolePageRequest;
import com.example.library.dto.request.UserPageRequest;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.PageResponse;
import com.example.library.dto.response.PaginationMeta;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private void validate(UserRequest request, Set<String> listCode, Set<Long> listRoleDB) {
        if(DataUtils.isBlank(request.getCode())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.CODE);
        } else if(DataUtils.maxLength(request.getCode(), UserConstant.MAX_LENGTH_CODE)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.CODE, UserConstant.CODE_LENGTH);
        } else if(!request.getCode().matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(ErrorCode.CODE_CHARACTER, UserConstant.CODE);
        } else if(listCode.contains(request.getCode().trim().toUpperCase())) {
            throw new BusinessException(ErrorCode.NOT_DUPLICATE, UserConstant.CODE);
        }
        if(DataUtils.isBlank(request.getFullName())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.FULL_NAME);
        } else if(DataUtils.maxLength(request.getFullName(), UserConstant.MAX_LENGTH)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.FULL_NAME, UserConstant.COMMON_LENGTH);
        }
        if(DataUtils.isEmptyList(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.LIST_ROLE);
        } else if(!listRoleDB.containsAll(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EXIST, UserConstant.NOT_EXIST_ROLE);
        }
    }

    @Override
    public ApiResponse<UserResponse> create(UserRequest request) {
        Set<String> listCode = userRepository.findAllCodes();
        Set<Long> listRoleDB = roleRepository.findAllId();
        validate(request, listCode, listRoleDB);
        if(DataUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.PASSWORD);
        } else if(DataUtils.maxLength(request.getFullName(), UserConstant.MAX_LENGTH_PASSWORD)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.PASSWORD, UserConstant.PASSWORD_LENGTH);
        }
        Set<Role> roles = new HashSet<>(
                roleRepository.findAllById(request.getListRole())
        );
        User user = User.builder()
                .code(request.getCode().trim().toUpperCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        User saved = userRepository.save(user);
        UserResponse response = userMapper.toUserResponse(saved);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<UserResponse> update(UserRequest request) {
        User user = userRepository.findByIdAndIsDeletedNot(request.getId(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, UserConstant.USER));
        Set<String> listCode = userRepository.findAllCodesOtherId(request.getId());
        Set<Long> listRoleDB = roleRepository.findAllId();
        validate(request, listCode, listRoleDB);
        Set<Role> roles = new HashSet<>(
                roleRepository.findAllById(request.getListRole())
        );
        user.setCode(request.getCode().trim().toUpperCase());
        user.setFullName(request.getFullName().trim());
        user.setRoles(roles);
        user.setUpdatedAt(LocalDateTime.now());
        user.setMfaEnabled(request.getMfaEnabled());
        user.setIsLocked(request.getIsLocked());
        userRepository.save(user);
        UserResponse response = userMapper.toUserResponse(user);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<Void> delete(String id) {
        User user = userRepository.findByIdAndIsDeletedNot(id, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, UserConstant.USER));
        user.setIsDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    private void validateSearch(UserPageRequest request) {
        if(DataUtils.isNull(request.getPage())) {
            request.setPage(0);
        }
        if(DataUtils.isNull(request.getSize())) {
            request.setSize(10);
        }
        if(DataUtils.isBlank(request.getSortBy())) {
            request.setSortBy(PermissionConstant.UPDATED_AT);
        }
        if(DataUtils.isBlank(request.getSortDir())) {
            request.setSortDir(PermissionConstant.DESC);
        }
        if(DataUtils.isEmptyList(request.getListRole())) {
            request.setListRole(null);
        }
    }

    @Override
    public ApiResponse<PageResponse<UserResponse>> search(UserPageRequest request) {
        validateSearch(request);
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Page<User> page = userRepository.search(request.getCode(), request.getFullName(), request.getEmail(), request.getListRole(), pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(userMapper::toUserResponse).toList();
        PaginationMeta contentPage = PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
        PageResponse<UserResponse> result = PageResponse.<UserResponse>builder()
                .content(content)
                .pagination(contentPage)
                .build();
        return ResponseUtils.success(result, AppConstant.SUCCESS);
    }
}
