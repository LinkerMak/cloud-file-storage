package com.linkermak.cloud_file_storage.controllers;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import com.linkermak.cloud_file_storage.controllers.cookie.CookieValueExtractor;
import com.linkermak.cloud_file_storage.controllers.cookie.SessionCookieBuilder;
import com.linkermak.cloud_file_storage.dto.web.authentication.LoginResult;
import com.linkermak.cloud_file_storage.dto.web.authentication.response.UsernameResponse;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignInRequest;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.services.authentication.UserAuthenticationService;
import com.linkermak.cloud_file_storage.services.authentication.UserRegisterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegisterService userRegisterService;
    private final UserAuthenticationService userAuthenticationService;

    private final SessionCookieBuilder sessionCookieBuilder;

    private final SessionProperties sessionProperties;

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponse> register(@Valid @RequestBody SignUpRequest signUpRequest,
                                                     HttpServletRequest servletRequest) {
        Optional<String> oldSessionId = CookieValueExtractor.
                extract(servletRequest, sessionProperties.getSessionCookieName());

        userRegisterService.register(signUpRequest);
        LoginResult loginResult = userAuthenticationService.login(signUpRequest);

        oldSessionId.ifPresent(id -> userAuthenticationService.deleteSession(id));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE,
                        sessionCookieBuilder.createSessionCookie(loginResult.sessionId()))
                .body(new UsernameResponse(loginResult.username()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UsernameResponse> login(@Valid @RequestBody SignInRequest request) {
        LoginResult loginResult = userAuthenticationService.login(request);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE,
                        sessionCookieBuilder.createSessionCookie(loginResult.sessionId()))
                .body(new UsernameResponse(loginResult.username()));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String sessionId = CookieValueExtractor.
                extract(request, sessionProperties.getSessionCookieName())
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Unauthorized"));

        userAuthenticationService.deleteSession(sessionId);
        SecurityContextHolder.clearContext();

        return ResponseEntity
                .noContent()
                .header(HttpHeaders.SET_COOKIE,
                        sessionCookieBuilder.createClearSessionCookie())
                .build();
    }
}
