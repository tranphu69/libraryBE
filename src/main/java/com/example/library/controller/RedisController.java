package com.example.library.controller;

import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.RedisSearchResponse;
import com.example.library.service.RedisService;
import com.example.library.util.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/redis")
@Tag(name = "Redis Management")
public class RedisController {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisService redisService;

    @GetMapping("/ping")
    public String pingRedis() {
        assert redisTemplate.getConnectionFactory() != null;
        return redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();
    }

    @GetMapping("/search")
    public ApiResponse<RedisSearchResponse> search(@RequestParam("key") String key) {
        String pattern = key.contains("*") ? key : "*" + key + "*";
        Map<String, Object> data = redisService.searchByPattern(pattern);
        RedisSearchResponse response = RedisSearchResponse.builder()
                .pattern(pattern)
                .totalKeys(data.size())
                .data(data)
                .build();
        return ResponseUtils.success(response, "SUCCESS");
    }
}
