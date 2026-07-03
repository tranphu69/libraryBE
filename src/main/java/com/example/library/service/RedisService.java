package com.example.library.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface RedisService {
    void blacklist(String jti, Instant expiresAt);
    boolean isBlacklisted(String jti);
    Map<String, Object> searchByPattern(String pattern);
    void set(String key, Object value, Duration ttl);
    <T> Optional<T> get(String key, Class<T> type);
    void delete(String key);
    Long getExpire(String key);
}
