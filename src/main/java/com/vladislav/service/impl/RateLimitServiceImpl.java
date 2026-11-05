package com.vladislav.service.impl;

import com.vladislav.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private static final String KEY_PREFIX = "rate_limit:create:";
    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;

    public boolean isRateLimited(String ip) {
        String key = KEY_PREFIX + ip;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                return false;
            }
            if (count == 1 || hasNoTtl(key)) {
                redisTemplate.expire(key, WINDOW);
            }
            return count > MAX_REQUESTS;
        } catch (RuntimeException e) {
            log.warn("Rate limiter is unavailable, skipping check for ip {}", ip, e);
            return false;
        }
    }

    private boolean hasNoTtl(String key) {
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl == -1;
    }
}
