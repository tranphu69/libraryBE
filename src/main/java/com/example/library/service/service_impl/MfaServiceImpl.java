package com.example.library.service.service_impl;

import com.example.library.domain.User;
import com.example.library.repository.UserRepository;
import com.example.library.service.MfaService;
import com.example.library.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
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

    private String generateNumericOtp(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String hmacHash(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(otpSignerKey.getBytes(), "HmacSHA256"));
            byte[] result = mac.doFinal(otp.getBytes());
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi hash OTP", e);
        }
    }

    @Override
    public String initiateOtpChallenge(User user) {
        String otp = generateNumericOtp(OTP_LENGTH);
        String challengeToken = UUID.randomUUID().toString();
        String key = KEY_PREFIX + challengeToken;
        redisService.set(key, hmacHash(otp), OTP_TTL);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return challengeToken;
    }
}
