package com.example.library.controller;

import com.example.library.dto.response.ApiResponse;
import com.example.library.dto.response.RedisSearchResponse;
import com.example.library.service.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisControllerTest {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private RedisService redisService;
    @Mock
    private RedisConnectionFactory connectionFactory;
    @Mock
    private RedisConnection connection;

    @InjectMocks
    private RedisController controller;

    @Test
    void pingRedisShouldReturnConnectionPing() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.ping()).thenReturn("PONG");

        String ping = controller.pingRedis();

        assertThat(ping).isEqualTo("PONG");
    }

    @Test
    void searchShouldReturnWrappedResponse() {
        when(redisService.searchByPattern("*abc*")).thenReturn(Map.of("key", "value"));

        ApiResponse<RedisSearchResponse> response = controller.search("abc");

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getData().getPattern()).isEqualTo("*abc*");
        assertThat(response.getData().getTotalKeys()).isEqualTo(1);
        assertThat(response.getData().getData()).containsEntry("key", "value");
        verify(redisService).searchByPattern("*abc*");
    }
}
