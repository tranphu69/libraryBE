package com.example.library.service;

import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    ApiResponse<AuthenticationResponse> logIn(AuthenticationRequest request);
}
