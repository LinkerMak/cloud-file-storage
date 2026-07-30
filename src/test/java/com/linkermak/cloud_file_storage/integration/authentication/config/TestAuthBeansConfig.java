package com.linkermak.cloud_file_storage.integration.authentication.config;

import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;
import com.linkermak.cloud_file_storage.integration.authentication.cleaner.SessionTestCleaner;
import com.linkermak.cloud_file_storage.repositories.storage.ResourceStorageRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

@TestConfiguration
public class TestAuthBeansConfig {

    @Bean
    SessionTestCleaner sessionTestCleaner(RedisTemplate<String, UserSession> redisTemplate) {
        return new SessionTestCleaner(redisTemplate);
    }

    @Bean
    ResourceStorageRepository resourceStorageRepositoryStub() {
        return new InMemoryResourceStorageRepository();
    }
}