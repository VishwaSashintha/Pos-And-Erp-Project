package com.gradge.erp.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed JWT blocklist for secure logout.
 *
 * When a user logs out, their JWT is added to this blocklist with a TTL equal
 * to the remaining token lifetime. Subsequent requests carrying that token are
 * rejected by JwtAuthenticationFilter before authentication proceeds.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtBlocklistService {

    private static final String BLOCKLIST_PREFIX = "jwt:blocklist:";

    private final StringRedisTemplate redisTemplate;

    /**
     * Adds a token to the blocklist.
     *
     * @param token     Raw JWT string (or its hash/JTI)
     * @param ttlMillis Remaining validity in milliseconds
     */
    public void addToBlocklist(String token, long ttlMillis) {
        String key = BLOCKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(ttlMillis));
        log.info("JWT added to blocklist, TTL={}ms", ttlMillis);
    }

    /**
     * Returns {@code true} if the token is currently in the blocklist (i.e. revoked).
     */
    public boolean isBlocked(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKLIST_PREFIX + token));
    }
}
