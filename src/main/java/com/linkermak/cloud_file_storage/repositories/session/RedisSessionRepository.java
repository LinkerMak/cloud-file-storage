package com.linkermak.cloud_file_storage.repositories.session;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisSessionRepository implements SessionRepository {

    private static final String KEY_PREFIX = "session:";

    private final RedisTemplate<String, UserSession> redisTemplate;

    private final SessionProperties sessionProperties;

    @Override
    public String save(UserSession session) {
        String uuid = UUID.randomUUID().toString();
        String key = KEY_PREFIX + uuid;

        redisTemplate.opsForValue().set(key,
                session,
                Duration.ofSeconds(sessionProperties.getTtlSeconds()));

        return uuid;
    }

    @Override
    public Optional<UserSession> findById(String sessionUUID) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(KEY_PREFIX + sessionUUID));
    }

    @Override
    public void delete(String sessionUUID) {
        redisTemplate.delete(KEY_PREFIX + sessionUUID);
    }

    @Override
    public Duration getRemainingTTL(String sessionUUID) {
        Long ttlSeconds = redisTemplate.getExpire(KEY_PREFIX + sessionUUID, TimeUnit.SECONDS);

        if(ttlSeconds == null) {
            throw new IllegalStateException("Session TTL is unavailable");
        }

        if(ttlSeconds < 0) {
            throw new IllegalStateException("Invalid session TTL:" + ttlSeconds);
        }

        return Duration.ofSeconds(ttlSeconds);
    }

    @Override
    public void refreshTTL(String sessionUUID) {
        redisTemplate.expire(
                KEY_PREFIX + sessionUUID,
                Duration.ofSeconds(sessionProperties.getTtlSeconds()));
    }
}
