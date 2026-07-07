package com.linkermak.cloud_file_storage.security.repositories;

import com.linkermak.cloud_file_storage.security.config.SessionProperties;
import com.linkermak.cloud_file_storage.security.dto.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RedisSessionRepository {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, UserSession> redisTemplate;

    private final SessionProperties sessionProperties;

    @Autowired
    public RedisSessionRepository(RedisTemplate<String, UserSession> redisTemplate, SessionProperties sessionProperties) {
        this.redisTemplate = redisTemplate;
        this.sessionProperties = sessionProperties;
    }

    public String save(UserSession session) {
        String uuid = UUID.randomUUID().toString();
        String key = KEY_PREFIX + uuid;

        redisTemplate.opsForValue().set(key,
                session,
                Duration.ofMinutes(sessionProperties.getTtlMinutes()));

        return uuid;
    }

    public Optional<UserSession> findById(String sessionUUID) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(KEY_PREFIX + sessionUUID));
    }

    public void delete(String sessionUUID) {
        redisTemplate.delete(KEY_PREFIX + sessionUUID);
    }

    public void refreshTTL(String sessionUUID) {
        redisTemplate.expire(
                KEY_PREFIX + sessionUUID,
                Duration.ofMinutes(sessionProperties.getTtlMinutes()));
    }
}
