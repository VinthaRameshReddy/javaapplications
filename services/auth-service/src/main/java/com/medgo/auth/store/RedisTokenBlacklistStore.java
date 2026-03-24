package com.medgo.auth.store;

import com.medgo.jwt.TokenBlacklistStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis-backed token blacklist store for auth-service.
 * Registers only when a StringRedisTemplate bean is available.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisTokenBlacklistStore implements TokenBlacklistStore {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "revoked:token:";

    @Override
    public void blacklist(String token, long secondsToExpire) {
        try {
            if (token == null || token.isBlank()) return;
            String key = KEY_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", Duration.ofSeconds(Math.max(1, secondsToExpire)));
            log.info("Blacklisted token with TTL seconds={}", secondsToExpire);
        } catch (Exception e) {
            log.error("Error blacklisting token in redis: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            if (token == null || token.isBlank()) return false;
            String key = KEY_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking token blacklist in redis: {}", e.getMessage(), e);
            return false;
        }
    }
}


