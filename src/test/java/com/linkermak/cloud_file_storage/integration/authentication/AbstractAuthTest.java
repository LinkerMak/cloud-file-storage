package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;
import com.linkermak.cloud_file_storage.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractAuthTest extends AbstractIntegrationTest {

    protected static final String USERNAME = "makar";
    protected static final String PASSWORD = "password123";

    protected static final String INVALID_SUFFIX = "!";

    @Autowired
    private PasswordEncoder passwordEncoder;

    protected User createUser() {
        return userRepository.save(new User(
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));
    }

    protected void assertSessionExists(String setCookie) {
        String sessionId = extractSessionId(setCookie);
        assertThat(sessionId).isNotBlank();

        Optional<UserSession> userSession = sessionRepository.findById(sessionId);
        assertThat(userSession).isPresent();
        assertThat(userSession.get().getUsername()).isEqualTo(USERNAME);
    }

    private String extractSessionId(String setCookie) {
        if (setCookie == null || setCookie.isBlank()) {
            return null;
        }

        String firstPart = setCookie.split(";", 2)[0];
        String[] keyValue = firstPart.split("=", 2);

        return keyValue.length == 2 ? keyValue[1] : null;
    }
}
