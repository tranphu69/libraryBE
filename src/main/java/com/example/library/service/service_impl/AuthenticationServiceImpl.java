package com.example.library.service.service_impl;

import com.example.library.constant.AppConstant;
import com.example.library.domain.Permission;
import com.example.library.domain.RefreshToken;
import com.example.library.domain.User;
import com.example.library.dto.request.*;
import com.example.library.dto.response.*;
import com.example.library.exception.BusinessException;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.mapper.UserMapper;
import com.example.library.repository.RefreshTokenRepository;
import com.example.library.repository.UserRepository;
import com.example.library.service.AuthenticationService;
import com.example.library.service.MfaService;
import com.example.library.service.RedisService;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HexFormat;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisService redisService;
    private final MfaService mfaService;
    @Value("${jwt.access-token-expiry}")
    protected long accessDuration;
    @Value("${jwt.refresh-token-expiry}")
    protected long refreshDuration;
    @Value("${jwt.access-signer-key}")
    protected String accessSignerKey;
    @Value("${jwt.refresh-signer-key}")
    protected String refreshSignerKey;
    private static final String FAILED_LOGIN_PREFIX = "login:failed:";
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration LOCK_TTL = Duration.ofMinutes(5);

    private String buildScope(User user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }

    private String generateRefreshToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getCode())
                .issuer("library.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(refreshDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(refreshSignerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_TOKEN, e.getMessage());
        }
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private String generateAccessToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getCode())
                .issuer("library.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(accessDuration, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(accessSignerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED_TOKEN, e.getMessage());
        }
    }

    private AuthenticationResponse issueTokens(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        RefreshToken token = refreshTokenRepository.findByUserId(user.getId())
                .orElseGet(RefreshToken::new);
        token.setTokenHash(hash(refreshToken));
        token.setUser(user);
        token.setExpiresAt(Instant.now().plus(refreshDuration, ChronoUnit.SECONDS));
        token.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .mfaRequired(user.getMfaEnabled())
                .build();
    }

    @Override
    @Transactional
    public ApiResponse<AuthenticationResponse> logIn(AuthenticationRequest request) {
        String username = request.getUsername();
        String failedKey = FAILED_LOGIN_PREFIX + username;
        Long attemptsSoFar = redisService.get(failedKey, Integer.class)
                .map(Long::valueOf)
                .orElse(0L);
        if (attemptsSoFar >= MAX_ATTEMPTS) {
            throw new ResourceNotFoundException(ErrorCode.ACCOUNT_LOCKED);
        }
        User user = userRepository.findByCodeAndIsDeletedNot(request.getUsername(), true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if(!authenticated) {
            long attempts = redisService.increment(failedKey, LOCK_TTL);
            if (attempts >= MAX_ATTEMPTS) {
                throw new ResourceNotFoundException(ErrorCode.ACCOUNT_LOCKED);
            }
            throw new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT);
        }
        redisService.delete(failedKey);
        if (user.getMfaEnabled()) {
            String challengeToken = mfaService.initiateOtpChallenge(user);
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .mfaRequired(true)
                    .challengeToken(challengeToken)
                    .build();
            return ResponseUtils.success(response, AppConstant.SUCCESS);
        }
        AuthenticationResponse authenticationResponse = issueTokens(user);
        return ResponseUtils.success(authenticationResponse, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<IntrospectResponse> introspect(IntrospectRequest request) throws JOSEException, ParseException {
        if(DataUtils.isBlank(request.getToken())) {
            throw new BusinessException(ErrorCode.NOT_TOKEN);
        }
        JWSVerifier verifier = new MACVerifier(accessSignerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(request.getToken());
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        if(!(signedJWT.verify(verifier) && expiryTime.after(new Date())))
            throw new BusinessException(ErrorCode.AUTHENTICATION_TOKEN);
        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        boolean isValid = !redisService.isBlacklisted(jti);
        IntrospectResponse response = new IntrospectResponse();
        response.setValid(isValid);
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

    private SignedJWT verifyAccessToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(accessSignerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(accessDuration, ChronoUnit.SECONDS).toEpochMilli());
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
        var signToken = verifyAccessToken(request.getToken());
        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();
        redisService.blacklist(jit, expiryTime.toInstant());
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User byUsername = userRepository.findByCodeAndIsDeletedNot(name, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        refreshTokenRepository.deleteByUserId(byUsername.getId());
        return ResponseUtils.success(null, AppConstant.SUCCESS);
    }

    @Override
    public ApiResponse<AuthenticationResponse> verifyOtpAndLogin(VerifyOtpRequest request) {
        User user = mfaService.verifyOtp(request.getChallengeToken(), request.getOtp());
        AuthenticationResponse response = issueTokens(user);
        return ResponseUtils.success(response, AppConstant.SUCCESS);
    }

    private SignedJWT verifyRefreshToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(refreshSignerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiryTime = new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(refreshDuration, ChronoUnit.SECONDS).toEpochMilli());
        if(!(signedJWT.verify(verifier) && expiryTime.after(new Date())))
            throw new BusinessException(ErrorCode.AUTHENTICATION_TOKEN);
        return signedJWT;
    }

    @Override
    public ApiResponse<AuthenticationResponse> refreshToken(RefreshRequest request) throws JOSEException, ParseException {
        if(DataUtils.isBlank(request.getAccessToken()) || DataUtils.isBlank(request.getRefreshToken())) {
            throw new BusinessException(ErrorCode.NOT_TOKEN);
        }
        var signToken = verifyRefreshToken(request.getRefreshToken());
        var accessToken = verifyAccessToken(request.getAccessToken());
        if (!refreshTokenRepository.existsByTokenHash(hash(request.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_TOKEN);
        }
        String jitAccessToken = accessToken.getJWTClaimsSet().getJWTID();
        Date expiryTimeAccessToken = accessToken.getJWTClaimsSet().getExpirationTime();
        redisService.blacklist(jitAccessToken, expiryTimeAccessToken.toInstant());
        String username = signToken.getJWTClaimsSet().getSubject();
        User user = userRepository.findByCodeAndIsDeletedNot(username, true)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
        AuthenticationResponse authenticationResponse = issueTokens(user);
        return ResponseUtils.success(authenticationResponse, AppConstant.SUCCESS);
    }
}
