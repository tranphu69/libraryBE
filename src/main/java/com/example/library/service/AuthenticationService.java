package com.example.library.service;

import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.request.IntrospectRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.dto.response.IntrospectResponse;
import com.example.library.dto.response.UserResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    ApiResponse<AuthenticationResponse> logIn(AuthenticationRequest request);
    ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws JOSEException, ParseException;
    ApiResponse<UserResponse> profile();
}
