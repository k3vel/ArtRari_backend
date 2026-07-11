package com.project.ArtRari.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String BANNED_PREFIX = "banned:";

    public void blacklistToken(String token, long durationTimeRemaining) {
        if (durationTimeRemaining > 0) {
            String key = BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", durationTimeRemaining, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    public void banUser(String email, long durationTimeRemaining) {
        if (durationTimeRemaining > 0) {
            String key = BANNED_PREFIX + email;
            redisTemplate.opsForValue().set(key, "banned", durationTimeRemaining, TimeUnit.MILLISECONDS);
        }
    }

    public void unbanUser(String email) {
        redisTemplate.delete(BANNED_PREFIX + email);
    }

    public boolean isBanned(String email) {
        return redisTemplate.hasKey(BANNED_PREFIX + email);
    }
}
