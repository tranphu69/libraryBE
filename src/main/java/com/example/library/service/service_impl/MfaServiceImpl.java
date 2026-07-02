package com.example.library.service.service_impl;

import com.example.library.domain.User;
import com.example.library.dto.Object.OtpChallenge;
import com.example.library.exception.ErrorCode;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.UserRepository;
import com.example.library.service.EmailService;
import com.example.library.service.MfaService;
import com.example.library.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class MfaServiceImpl implements MfaService {
    private final RedisService redisService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${jwt.otp-signer-key}")
    protected String otpSignerKey;
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_TTL = Duration.ofMinutes(2);
    private static final int MAX_ATTEMPTS = 5;
    private static final String KEY_PREFIX = "mfa:otp:";

    private String generateNumericOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MfaServiceImpl.OTP_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String hmacHash(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpSignerKey.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(otp.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hash OTP", e);
        }
    }

    @Override
    public String initiateOtpChallenge(User user) {
        String otp = generateNumericOtp();
        String challengeToken = UUID.randomUUID().toString();
        OtpChallenge challenge = OtpChallenge.builder()
                .userId(user.getId())
                .otpHash(hmacHash(otp))
                .attemptCount(0)
                .build();
        redisService.set(KEY_PREFIX + challengeToken, challenge, OTP_TTL);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return challengeToken;
    }

    @Override
    public User verifyOtp(String challengeToken, String otp) {
        String key = KEY_PREFIX + challengeToken;
        OtpChallenge challenge = redisService.get(key, OtpChallenge.class)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MFA_CHALLENGE_INVALID));
        if (challenge.getAttemptCount() >= MAX_ATTEMPTS) {
            redisService.delete(key);
            throw new ResourceNotFoundException(ErrorCode.MFA_TOO_MANY_ATTEMPTS);
        }
        if (!hmacHash(otp).equals(challenge.getOtpHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            Long ttl = redisService.getExpire(key);
            redisService.set(key, challenge,
                    Duration.ofSeconds(ttl != null && ttl > 0 ? ttl : OTP_TTL.getSeconds()));
            throw new ResourceNotFoundException(ErrorCode.MFA_OTP_INVALID);
        }
        redisService.delete(key);
        return userRepository.findById(challenge.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.AUTHENTICATION_ACCOUNT));
    }
}
