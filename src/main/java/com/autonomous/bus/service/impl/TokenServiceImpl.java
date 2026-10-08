package com.autonomous.bus.service.impl;

import com.autonomous.bus.service.TokenService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenServiceImpl implements TokenService {

    private static final String TOKEN_PREFIX = "auth:token:";
    private static final long EXPIRE_HOURS = 24;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveToken(Long userId, String token) {
        redisTemplate.opsForValue().set(TOKEN_PREFIX + userId, token, EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public String getToken(Long userId) {
        Object value = redisTemplate.opsForValue().get(TOKEN_PREFIX + userId);
        return value != null ? value.toString() : null;
    }

    @Override
    public void deleteToken(Long userId) {
        redisTemplate.delete(TOKEN_PREFIX + userId);
    }

    @Override
    public boolean validateToken(Long userId, String token) {
        String storedToken = getToken(userId);
        return storedToken != null && storedToken.equals(token);
    }
}
