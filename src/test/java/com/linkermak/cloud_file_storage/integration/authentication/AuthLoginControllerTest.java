package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignInRequest;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.messages.ValidationMessages;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.linkermak.cloud_file_storage.advices.authentication.messages.SecurityExceptionMessages.INVALID_USERNAME_OR_PASSWORD_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthLoginControllerTest extends AbstractAuthTest {

    private static final String INVALID_LONG_SUFFIX =
            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";

    @Test
    void loginReturns200AndSetCookieAndSessionExists() throws Exception {
        createUser();

        SignInRequest request = createValidSignInRequest();

        String setCookie = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.username").value(USERNAME))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);

        assertThat(setCookie).isNotBlank();

        assertSessionExists(setCookie);

        assertThat(userRepository.existsByUsername(USERNAME)).isTrue();
    }

    @Test
    void loginReturns401WhenInvalidCredentials() throws Exception {
        createUser();

        SignInRequest request = createInvalidCredentialsSignInRequest();

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(INVALID_USERNAME_OR_PASSWORD_MESSAGE));


        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(sessionRepository.count()).isEqualTo(0);

    }

    @Test
    void loginReturns400WhenUsernameIsInvalid() throws Exception {
        SignInRequest request = createLongUsernameSignInRequest();

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ValidationMessages.USERNAME_SIZE));
    }

    @Test
    void loginReturns400WhenPasswordIsInvalid() throws Exception {
        SignInRequest request = createLongPasswordSignInRequest();

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ValidationMessages.PASSWORD_SIZE));
    }

    private SignInRequest createInvalidCredentialsSignInRequest() {
        return new SignInRequest(
                USERNAME + INVALID_SUFFIX,
                PASSWORD
        );
    }

    private SignInRequest createLongPasswordSignInRequest() {
        return new SignInRequest(
                USERNAME,
                PASSWORD + INVALID_LONG_SUFFIX
        );
    }

    private SignInRequest createLongUsernameSignInRequest() {
        return new SignInRequest(
                USERNAME + INVALID_LONG_SUFFIX,
                PASSWORD
        );
    }
}
