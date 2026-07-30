package com.linkermak.cloud_file_storage.integration.authentication.cleaner;

import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@TestComponent
public class SessionTestCleaner {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, UserSession> redisTemplate;

    public SessionTestCleaner(RedisTemplate<String, UserSession> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void deleteAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}