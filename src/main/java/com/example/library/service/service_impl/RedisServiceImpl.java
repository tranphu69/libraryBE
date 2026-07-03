package com.example.library.service.service_impl;

import com.example.library.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    private static final String PREFIX = "blacklist:access:";
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void blacklist(String jti, Instant expiresAt) {
        long ttlSeconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        if(ttlSeconds <= 0) return;
        redisTemplate.opsForValue().set(PREFIX + jti, "1", Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public boolean isBlacklisted(String jti) {
        return redisTemplate.hasKey(PREFIX + jti);
    }

    @Override
    public Map<String, Object> searchByPattern(String pattern) {
        Map<String, Object> result = new LinkedHashMap<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();
        assert redisTemplate.getConnectionFactory() != null;
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options)) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Object value = redisTemplate.opsForValue().get(key);
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void set(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) return Optional.empty();
        return Optional.of(type.cast(value));
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
}
