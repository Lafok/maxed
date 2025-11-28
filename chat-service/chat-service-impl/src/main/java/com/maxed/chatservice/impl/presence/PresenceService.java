package com.maxed.chatservice.impl.presence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final StringRedisTemplate redisTemplate;

    private static final String ONLINE_PREFIX = "user:online:";
    private static final Duration TTL = Duration.ofMinutes(5);

    public void setUserOnline(Long userId) {
        redisTemplate.opsForValue().set(ONLINE_PREFIX + userId, "true", TTL);
    }

    public void setUserOffline(Long userId) {
        redisTemplate.delete(ONLINE_PREFIX + userId);
    }

    public boolean isUserOnline(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(ONLINE_PREFIX + userId));
    }

    public Map<Long, Boolean> getUsersOnlineStatus(Collection<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();

        List<String> keys = userIds.stream()
                .map(id -> ONLINE_PREFIX + id)
                .toList();

        List<String> results = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Boolean> statusMap = new java.util.HashMap<>();
        int i = 0;
        for (Long userId : userIds) {
            String val = results.get(i++);
            statusMap.put(userId, val != null);
        }
        return statusMap;
    }
}
