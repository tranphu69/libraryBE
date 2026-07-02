package com.example.library.service;

import java.time.Instant;
import java.util.Map;

public interface RedisService {
    void blacklist(String jti, Instant expiresAt);
    boolean isBlacklisted(String jti);
    Map<String, Object> searchByPattern(String pattern);
}
