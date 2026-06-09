package com.example.library.service.serviceImpl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.UserConstant;
import com.example.library.domain.Role;
import com.example.library.domain.User;
import com.example.library.dto.request.UserRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.RoleRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.UserService;
import com.example.library.util.DataUtils;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private void validate(UserRequest request, Set<String> listCode, Set<String> listEmail, Set<Long> listRoleDB) {
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
        if(DataUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.PASSWORD);
        } else if(DataUtils.maxLength(request.getFullName(), UserConstant.MAX_LENGTH_PASSWORD)) {
            throw new BusinessException(ErrorCode.NOT_LENGTH, UserConstant.PASSWORD, UserConstant.PASSWORD_LENGTH);
        }
        if(DataUtils.isEmptyList(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EMPTY, UserConstant.LIST_ROLE);
        } else if(!listRoleDB.containsAll(request.getListRole())) {
            throw new BusinessException(ErrorCode.NOT_EXIST, UserConstant.NOT_EXIST_ROLE);
        }
    }

    public ApiResponse<UserResponse> create(UserRequest request) {
        Set<String> listCode = userRepository.findAllCodes();
        Set<String> listEmail = userRepository.findAllEmails();
        Set<Long> listRoleDB = roleRepository.findAllId();
        validate(request, listCode, listEmail, listRoleDB);
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
}
