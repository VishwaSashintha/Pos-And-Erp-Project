package com.gradge.erp.security.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private static final int LIMIT = 100;
    private static final long WINDOW_SECONDS = 60;

    public boolean isAllowed(String key) {
        String redisKey = "rate_limit:" + key;

        Long currentCount = redisTemplate.opsForValue().increment(redisKey);

        // If it's the first request, set the expiration window
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(redisKey, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        return currentCount != null && currentCount <= LIMIT;
    }
}
