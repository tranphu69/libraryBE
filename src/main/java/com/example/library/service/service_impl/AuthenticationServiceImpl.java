package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.constant.UserConstant;
import com.example.library.domain.User;
import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthenticationService;
import com.example.library.util.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public ApiResponse<AuthenticationResponse> logIn(AuthenticationRequest request) {
        User user = userRepository.findByCodeAndIsDeletedNot(request.getUsername(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST, UserConstant.USER));
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .authenticated(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .build();
        return ResponseUtils.success(authenticationResponse, AppConstant.SUCCESS);
    }
}
