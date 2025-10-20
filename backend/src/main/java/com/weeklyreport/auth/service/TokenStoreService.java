package com.weeklyreport.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;

@Service
public class TokenStoreService {

    private static final String ACCESS_TOKEN_KEY_PREFIX = "auth:access:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh:";
    private static final String USER_ACCESS_SET_PREFIX = "auth:user:access:";
    private static final String USER_REFRESH_SET_PREFIX = "auth:user:refresh:";

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public TokenStoreService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeAccessToken(String username, String token, long ttlSeconds) {
        storeToken(ACCESS_TOKEN_KEY_PREFIX, USER_ACCESS_SET_PREFIX, username, token, ttlSeconds);
    }

    public void storeRefreshToken(String username, String token, long ttlSeconds) {
        storeToken(REFRESH_TOKEN_KEY_PREFIX, USER_REFRESH_SET_PREFIX, username, token, ttlSeconds);
    }

    public String removeAccessToken(String token) {
        return removeToken(ACCESS_TOKEN_KEY_PREFIX, USER_ACCESS_SET_PREFIX, token);
    }

    public String removeRefreshToken(String token) {
        return removeToken(REFRESH_TOKEN_KEY_PREFIX, USER_REFRESH_SET_PREFIX, token);
    }

    public boolean isAccessTokenValid(String token) {
        return hasToken(ACCESS_TOKEN_KEY_PREFIX + token);
    }

    public boolean isRefreshTokenValid(String token) {
        return hasToken(REFRESH_TOKEN_KEY_PREFIX + token);
    }

    public String getUsernameForAccessToken(String token) {
        return getUsername(ACCESS_TOKEN_KEY_PREFIX + token);
    }

    public String getUsernameForRefreshToken(String token) {
        return getUsername(REFRESH_TOKEN_KEY_PREFIX + token);
    }

    public void revokeUserTokens(String username) {
        removeTokensForUser(USER_ACCESS_SET_PREFIX, ACCESS_TOKEN_KEY_PREFIX, username);
        removeTokensForUser(USER_REFRESH_SET_PREFIX, REFRESH_TOKEN_KEY_PREFIX, username);
    }

    private void storeToken(String tokenPrefix,
                            String userSetPrefix,
                            String username,
                            String token,
                            long ttlSeconds) {
        String tokenKey = tokenPrefix + token;
        redisTemplate.opsForValue().set(tokenKey, username, Duration.ofSeconds(ttlSeconds));

        String userSetKey = userSetPrefix + username;
        redisTemplate.opsForSet().add(userSetKey, token);
        redisTemplate.expire(userSetKey, Duration.ofSeconds(ttlSeconds));
    }

    private String removeToken(String tokenPrefix,
                               String userSetPrefix,
                               String token) {
        String tokenKey = tokenPrefix + token;
        String username = redisTemplate.opsForValue().get(tokenKey);
        redisTemplate.delete(tokenKey);

        if (username != null) {
            redisTemplate.opsForSet().remove(userSetPrefix + username, token);
        }
        return username;
    }

    private boolean hasToken(String tokenKey) {
        Boolean hasKey = redisTemplate.hasKey(tokenKey);
        return hasKey != null && hasKey;
    }

    private String getUsername(String tokenKey) {
        return redisTemplate.opsForValue().get(tokenKey);
    }

    private void removeTokensForUser(String userSetPrefix,
                                     String tokenPrefix,
                                     String username) {
        String userSetKey = userSetPrefix + username;
        Set<String> tokens = redisTemplate.opsForSet().members(userSetKey);
        if (tokens == null) {
            tokens = Collections.emptySet();
        }
        for (String token : tokens) {
            if (token != null) {
                redisTemplate.delete(tokenPrefix + token);
            }
        }
        redisTemplate.delete(userSetKey);
    }
}
