package com.example.solex_backend.service;

import com.example.solex_backend.domain.User;
import com.example.solex_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final RedisTemplate<String, User> userRedisTemplate;
    private final UserRepository userRepository;

    private static final Duration TTL = Duration.ofMinutes(30);
    private static final String KEY_PREFIX = "user:cache:";

    public User getOrLoad(Long userId) {
        String key = KEY_PREFIX + userId;
        User cached = userRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRedisTemplate.opsForValue().set(key, user, TTL);
        }
        return user;
    }

    public void evict(Long userId) {
        userRedisTemplate.delete(KEY_PREFIX + userId);
    }
}
