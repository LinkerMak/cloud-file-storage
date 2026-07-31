package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignInRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthLogoutControllerTest extends AbstractAuthTest {

    @Autowired
    private SessionProperties sessionProperties;

    @Test
    void logoutReturns204AndClearsCookieAndDeletesSession() throws Exception {
        createUser();

        Cookie sessionCookie = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidSignInRequest())))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                        .andReturn()
                        .getResponse()
                        .getCookie(sessionProperties.getSessionCookieName());

        assertThat(sessionCookie).isNotNull();
        assertThat(sessionCookie.getValue()).isNotBlank();
        assertThat(sessionRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(sessionCookie))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("SESSION_ID"))
                .andExpect(cookie().maxAge("SESSION_ID", 0));

        assertThat(sessionRepository.count()).isEqualTo(0);

        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(sessionCookie))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutReturns401WhenSessionCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/sign-out")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isUnauthorized());

        assertThat(sessionRepository.count()).isEqualTo(0);
    }

    private SignInRequest createValidSignInRequest() {
        return new SignInRequest(
                USERNAME,
                PASSWORD
        );
    }
}
