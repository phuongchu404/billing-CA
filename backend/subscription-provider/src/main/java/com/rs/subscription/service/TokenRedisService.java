package com.rs.subscription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenRedisService {

    private static final String AT_PREFIX  = "sms:at:";   // access token whitelist
    private static final String RT_AT_PREFIX = "sms:rt_at:"; // refresh token → access token mapping

    private final StringRedisTemplate redisTemplate;

    /**
     * Called on login / token refresh.
     * Stores the access token in the whitelist and links the refresh token to it.
     */
    public void storeTokens(String accessToken, String refreshToken, String userId,
                            long accessTtlSeconds, long refreshTtlSeconds) {
        try {
            redisTemplate.opsForValue().set(AT_PREFIX + accessToken, userId, accessTtlSeconds, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(RT_AT_PREFIX + refreshToken, accessToken, refreshTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis storeTokens failed: {}", e.getMessage());
        }
    }

    /**
     * Called on token refresh: rotate the access token linked to an existing refresh token.
     */
    public void rotateAccessToken(String refreshToken, String newAccessToken, String userId,
                                  long accessTtlSeconds) {
        try {
            String oldAt = redisTemplate.opsForValue().get(RT_AT_PREFIX + refreshToken);
            if (oldAt != null) {
                redisTemplate.delete(AT_PREFIX + oldAt);
            }
            redisTemplate.opsForValue().set(AT_PREFIX + newAccessToken, userId, accessTtlSeconds, TimeUnit.SECONDS);
            // Update mapping — keep existing TTL on the refresh token key
            Long remainingTtl = redisTemplate.getExpire(RT_AT_PREFIX + refreshToken, TimeUnit.SECONDS);
            long ttl = (remainingTtl != null && remainingTtl > 0) ? remainingTtl : accessTtlSeconds;
            redisTemplate.opsForValue().set(RT_AT_PREFIX + refreshToken, newAccessToken, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Redis rotateAccessToken failed: {}", e.getMessage());
        }
    }

    /**
     * Returns true if the access token is present in Redis (i.e., valid session).
     * Fails open on Redis errors so an outage does not break auth entirely.
     */
    public boolean isAccessTokenValid(String accessToken) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(AT_PREFIX + accessToken));
        } catch (Exception e) {
            log.warn("Redis unavailable during token check — falling back to JWT-only validation: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Called on logout: deletes the access token and the refresh→access mapping.
     */
    public void revokeByRefreshToken(String refreshToken) {
        try {
            String at = redisTemplate.opsForValue().get(RT_AT_PREFIX + refreshToken);
            if (at != null) {
                redisTemplate.delete(AT_PREFIX + at);
            }
            redisTemplate.delete(RT_AT_PREFIX + refreshToken);
        } catch (Exception e) {
            log.error("Redis revokeByRefreshToken failed: {}", e.getMessage());
        }
    }
}
