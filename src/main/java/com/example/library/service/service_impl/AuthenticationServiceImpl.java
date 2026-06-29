package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.domain.Permission;
import com.example.library.domain.User;
import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.request.IntrospectRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.dto.response.IntrospectResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthenticationService;
import com.example.library.util.ResponseUtils;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    @Value("${jwt.access-token-expiry}")
    protected long validDuration;
    @Value("${jwt.signerKey}")
    protected String signerKey;

    private String buildScope(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getCode())
                .issuer("library.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthenticationResponse> logIn(AuthenticationRequest request) {
        User user = userRepository.findByCodeAndIsDeletedNot(request.getUsername(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated) {
            throw new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT);
        }
        String token = generateToken(user);
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(token)
                .build();
        return ResponseUtils.success(authenticationResponse, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        IntrospectResponse response = IntrospectResponse.builder()
                .valid(signedJWT.verify(verifier) && expiryTime.after(new Date()))
                .build();
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<UserResponse> profile() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User byUsername = userRepository.findByCodeAndIsDeletedNot(name, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        UserResponse response = userMapper.toUserResponse(byUsername);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }
}
