package com.example.library.controller;

import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.request.IntrospectRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.dto.response.IntrospectResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authentication")
@Tag(name = "Authentication Management")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> logIn(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.logIn(request));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) throws ParseException, JOSEException {
        return ResponseEntity.ok(authenticationService.introspect(request));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> profile() {
        return ResponseEntity.ok(authenticationService.profile());
    }
}
