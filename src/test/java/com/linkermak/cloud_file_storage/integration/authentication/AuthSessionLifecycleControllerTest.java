package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthSessionLifecycleControllerTest extends AbstractAuthTest {

    @Autowired
    private SessionProperties sessionProperties;

    @Test
    void sessionLifecycleShouldAllowMeAndDenyAfterLogout() throws Exception {
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

        mockMvc.perform(get("/api/user/me")
                        .cookie(sessionCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(USERNAME));

        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(sessionCookie))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertThat(sessionRepository.count()).isEqualTo(0);

        mockMvc.perform(get("/api/user/me")
                        .cookie(sessionCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userMeReturns401WithoutSession() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userMeReturns401WithInvalidSession() throws Exception {
        Cookie invalidSessionCookie = new Cookie(
                sessionProperties.getSessionCookieName(),
                "invalid-session-id"
        );

        mockMvc.perform(get("/api/user/me")
                        .cookie(invalidSessionCookie)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}
