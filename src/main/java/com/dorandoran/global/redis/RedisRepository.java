package com.dorandoran.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

import static com.dorandoran.global.jwt.JWTConstant.REFRESH_TOKEN_HEADER;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveRefreshToken(String userId, String refreshToken, long expiredTime) {
        String key = keyGenerator(userId);
        save(key, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String userId) {
        String key = keyGenerator(userId);
        return (String) get(key);
    }

    public void deleteRefreshToken(String userId) {
        String key = keyGenerator(userId);
        delete(key);
    }

    private static String keyGenerator(String userId) {
        return String.format("%s_%s", REFRESH_TOKEN_HEADER, userId);
    }

    private void save(String key, Object value, long duration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, duration, timeUnit);
    }

    private Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    private void delete(String key) {
        redisTemplate.delete(key);
    }

    public void saveEmailAuthCode(String email, String authCode) {
        String key = "EMAIL_AUTH:" + email;
        redisTemplate.opsForValue().set(key, authCode, 5, TimeUnit.MINUTES);
    }

    public String getAuthCode(String email) {
        String key = "EMAIL_AUTH:" + email;
        return (String) redisTemplate.opsForValue().get(key);
    }

    public void deleteAuthCode(String email) {
        String key = "EMAIL_AUTH:" + email;
        redisTemplate.delete(key);
    }

    public void saveEmailVerified(String email) {
        String key = "EMAIL_VERIFIED:" + email;
        redisTemplate.opsForValue().set(key, "VERIFIED", 10, TimeUnit.MINUTES);
    }

    public boolean isEmailVerified(String email) {
        String key = "EMAIL_VERIFIED:" + email;
        return "VERIFIED".equals(redisTemplate.opsForValue().get(key));
    }

    public void deleteEmailVerified(String email) {
        String key = "EMAIL_VERIFIED:" + email;
        redisTemplate.delete(key);
    }
}
