package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.domain.Permission;
import com.example.library.domain.InvalidatedToken;
import com.example.library.domain.User;
import com.example.library.dto.request.AuthenticationRequest;
import com.example.library.dto.request.IntrospectRequest;
import com.example.library.dto.request.LogoutRequest;
import com.example.library.dto.request.RefreshRequest;
import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.AuthenticationResponse;
import com.example.library.dto.response.IntrospectResponse;
import com.example.library.dto.response.UserResponse;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.InvalidatedTokenRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthenticationService;
import com.example.library.util.DataUtils;
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
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    @Value("${jwt.access-token-expiry}")
    protected long validDuration;
    @Value("${jwt.refresh-token-expiry}")
    protected long refreshableDuration;
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
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_TOKEN, e);
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
        if(DataUtils.isBlank(request.getToken())) {
            throw new BusinessException(ErrorCode.NOT_TOKEN);
        }
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(signedJWT.verify(verifier) && expiryTime.after(new Date())))
            throw new BusinessException(ErrorCode.AUTHENTICATION_TOKEN);
        IntrospectResponse response = new IntrospectResponse();
        response.setValid(!invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()));
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

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(refreshableDuration, ChronoUnit.SECONDS).toEpochMilli());
        if(!(signedJWT.verify(verifier) && expiryTime.after(new Date())))
            throw new BusinessException(ErrorCode.AUTHENTICATION_TOKEN);
        return signedJWT;
    }

    @Override
    @Transactional
    public ApiResponse<Void> logout(LogoutRequest request) throws ParseException, JOSEException {
        if(DataUtils.isBlank(request.getToken())) {
            throw new BusinessException(ErrorCode.NOT_TOKEN);
        }
        var signToken = verifyToken(request.getToken());
        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .jti(jit)
                .expiresAt(expiryTime.toInstant())
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    public ApiResponse<AuthenticationResponse> refreshToken(RefreshRequest request) throws JOSEException, ParseException {
        var signToken = verifyToken(request.getToken());
        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .jti(jit)
                .expiresAt(expiryTime.toInstant())
                .build();
        invalidatedTokenRepository.save(invalidatedToken);
        String username = signToken.getJWTClaimsSet().getSubject();
        User user = userRepository.findByCodeAndIsDeletedNot(username, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        String token = generateToken(user);
        AuthenticationResponse authenticationResponse = AuthenticationResponse.builder()
                .token(token)
                .build();
        return ResponseUtils.success(authenticationResponse, AppConstant.SUCCESS);
    }
}
