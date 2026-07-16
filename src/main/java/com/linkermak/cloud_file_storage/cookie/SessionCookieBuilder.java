package com.linkermak.cloud_file_storage.cookie;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class SessionCookieBuilder {

    private final SessionProperties sessionProperties;

    public String createSessionCookie(String sessionId) {
        return ResponseCookie
                .from(sessionProperties.getSessionCookieName(), sessionId)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(sessionProperties.getTtlMinutes()))
                .sameSite("Lax")
                .build().toString();
    }

    public String createClearSessionCookie() {
        return ResponseCookie
                .from(sessionProperties.getSessionCookieName(), "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build().toString();

    }

}
