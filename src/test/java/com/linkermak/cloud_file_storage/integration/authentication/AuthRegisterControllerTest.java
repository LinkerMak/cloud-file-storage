package com.linkermak.cloud_file_storage.integration.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.messages.ValidationMessages;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static com.linkermak.cloud_file_storage.advices.authentication.messages.SecurityExceptionMessages.USER_ALREADY_EXISTS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthRegisterControllerTest extends AbstractAuthTest {

    @Test
    void registerReturns201AndSetCookieAnsSessionExists() throws Exception {
        SignUpRequest request = createValidSignUpRequest();

        String setCookie = mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
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
    void registerReturns400WhenIncorrectPassword() throws Exception {
        SignUpRequest request = createInvalidPasswordSignUpRequest();

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ValidationMessages.PASSWORD_INVALID_FORMAT));

        assertThat(userRepository.existsByUsername(USERNAME)).isFalse();
    }

    @Test
    void registerReturns400WhenIncorrectUsername() throws Exception {
        SignUpRequest request = createInvalidUsernameSignUpRequest();

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ValidationMessages.USERNAME_INVALID_FORMAT));

        assertThat(userRepository.existsByUsername(USERNAME)).isFalse();
    }

    @Test
    void registerReturns409WhenUserAlreadyExists() throws Exception {
        createUser();

        SignUpRequest request = createValidSignUpRequest();

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(USER_ALREADY_EXISTS_MESSAGE));

        assertThat(userRepository.count()).isEqualTo(1);
    }

    private SignUpRequest createValidSignUpRequest() {
        return new SignUpRequest(
                USERNAME,
                PASSWORD
        );
    }

    private SignUpRequest createInvalidPasswordSignUpRequest() {
        return new SignUpRequest(
                USERNAME,
                PASSWORD + INVALID_SUFFIX
        );
    }

    private SignUpRequest createInvalidUsernameSignUpRequest() {
        return new SignUpRequest(
                USERNAME + INVALID_SUFFIX,
                PASSWORD
        );
    }
}
