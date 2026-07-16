package com.linkermak.cloud_file_storage.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.redis.session")
public class SessionProperties {
    private long ttlMinutes = 30;
    private String sessionCookieName = "SESSION_ID";
}