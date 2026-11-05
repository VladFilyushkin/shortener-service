package com.vladislav.service;

import com.vladislav.service.impl.RateLimitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    private static final String IP = "127.0.0.1";
    private static final String KEY = "rate_limit:create:" + IP;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitServiceImpl rateLimitService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldNotBlockFirstRequest() {
        when(valueOperations.increment(KEY)).thenReturn(1L);

        assertFalse(rateLimitService.isRateLimited(IP));
    }

    @Test
    void shouldNotBlockTenthRequest() {
        when(valueOperations.increment(KEY)).thenReturn(10L);
        when(redisTemplate.getExpire(KEY)).thenReturn(30L);

        assertFalse(rateLimitService.isRateLimited(IP));
    }

    @Test
    void shouldBlockEleventhRequest() {
        when(valueOperations.increment(KEY)).thenReturn(11L);
        when(redisTemplate.getExpire(KEY)).thenReturn(30L);

        assertTrue(rateLimitService.isRateLimited(IP));
    }

    @Test
    void shouldSetTtlOnFirstRequest() {
        when(valueOperations.increment(KEY)).thenReturn(1L);

        rateLimitService.isRateLimited(IP);

        verify(redisTemplate).expire(KEY, WINDOW);
    }

    @Test
    void shouldNotResetTtlOnSubsequentRequests() {
        when(valueOperations.increment(KEY)).thenReturn(5L);
        when(redisTemplate.getExpire(KEY)).thenReturn(30L);

        rateLimitService.isRateLimited(IP);

        verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }

    @Test
    void shouldRestoreTtlWhenItWasLost() {
        when(valueOperations.increment(KEY)).thenReturn(5L);
        when(redisTemplate.getExpire(KEY)).thenReturn(-1L);

        rateLimitService.isRateLimited(IP);

        verify(redisTemplate).expire(KEY, WINDOW);
    }

    @Test
    void shouldNotBlockWhenRedisIsUnavailable() {
        when(valueOperations.increment(KEY))
                .thenThrow(new RedisConnectionFailureException("Redis is down"));

        assertFalse(rateLimitService.isRateLimited(IP));
    }
}
