package com.linkermak.cloud_file_storage.security.controllers;

import com.linkermak.cloud_file_storage.security.config.SessionProperties;
import com.linkermak.cloud_file_storage.security.dto.response.UsernameResponse;
import com.linkermak.cloud_file_storage.security.dto.signrequest.SignInRequest;
import com.linkermak.cloud_file_storage.security.dto.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.security.services.UserAuthenticationService;
import com.linkermak.cloud_file_storage.security.services.UserRegisterService;
import com.linkermak.cloud_file_storage.security.utils.CookieValueExtractor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRegisterService userRegisterService;
    private final UserAuthenticationService userAuthenticationService;

    private final SessionProperties sessionProperties;

    @Autowired
    public AuthController(UserRegisterService userRegisterService, UserAuthenticationService userAuthenticationService, SessionProperties sessionProperties) {
        this.userRegisterService = userRegisterService;
        this.userAuthenticationService = userAuthenticationService;
        this.sessionProperties = sessionProperties;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UsernameResponse> register(@Valid @RequestBody SignUpRequest signUpRequest,
                                                     HttpServletRequest servletRequest) {
        Optional<String> oldSessionId = CookieValueExtractor.
                extract(servletRequest, sessionProperties.getSessionCookieName());

        userRegisterService.register(signUpRequest);
        UserAuthenticationService.LoginResult loginResult = userAuthenticationService.login(signUpRequest);

        oldSessionId.ifPresent(id -> userAuthenticationService.deleteSession(id));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, createSessionCookie(loginResult.sessionId()))
                .body(new UsernameResponse(loginResult.username()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UsernameResponse> login(@Valid @RequestBody SignInRequest request) {
        UserAuthenticationService.LoginResult loginResult = userAuthenticationService.login(request);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, createSessionCookie(loginResult.sessionId()))
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
                .header(HttpHeaders.SET_COOKIE, createClearSessionCookie())
                .build();
    }

    private String createSessionCookie(String sessionId) {
        return ResponseCookie
                .from(sessionProperties.getSessionCookieName(), sessionId)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(sessionProperties.getTtlMinutes()))
                .sameSite("Lax")
                .build().toString();
    }

    private String createClearSessionCookie() {
        return ResponseCookie
                .from(sessionProperties.getSessionCookieName(), "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build().toString();

    }
}
