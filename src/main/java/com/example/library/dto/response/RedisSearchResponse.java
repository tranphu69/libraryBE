package com.example.library.dto.response;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedisSearchResponse {
    private String pattern;
    private int totalKeys;
    private Map<String, Object> data;
}
