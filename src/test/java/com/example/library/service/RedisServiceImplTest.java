package com.example.library.service;

import com.example.library.service.service_impl.RedisServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisServiceImplTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisServiceImpl service;

    @Test
    void getShouldReturnOptionalValue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("k1")).thenReturn("v1");

        Optional<String> value = service.get("k1", String.class);

        assertThat(value).contains("v1");
    }

    @Test
    void setShouldDelegateToRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.set("k1", "v1", Duration.ofSeconds(10));

        verify(valueOperations).set("k1", "v1", Duration.ofSeconds(10));
    }
}
