package com.example.library.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redis")
@Tag(name = "Redis Management")
public class RedisController {
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/ping")
    public String pingRedis() {
        assert redisTemplate.getConnectionFactory() != null;
        return redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
    }
}
