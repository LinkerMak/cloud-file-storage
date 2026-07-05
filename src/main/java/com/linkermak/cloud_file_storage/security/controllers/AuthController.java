package com.linkermak.cloud_file_storage.security.controllers;

import com.linkermak.cloud_file_storage.security.config.SessionProperties;
import com.linkermak.cloud_file_storage.security.dto.SignInRequest;
import com.linkermak.cloud_file_storage.security.dto.SignUpRequest;
import com.linkermak.cloud_file_storage.security.models.User;
import com.linkermak.cloud_file_storage.security.services.UserAuthenticationService;
import com.linkermak.cloud_file_storage.security.services.UserRegisterService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final static String SESSION_COOKIE_NAME = "SESSION_ID";

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
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody SignUpRequest request) {
        User user = userRegisterService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername()));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody SignInRequest request, HttpServletResponse response) {
        UserAuthenticationService.LoginResult loginResult = userAuthenticationService.login(request);

        response.addHeader(HttpHeaders.SET_COOKIE, collectCookie(loginResult.sessionId()).toString());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("username", loginResult.username()));
    }

    private ResponseCookie collectCookie(String sessionId) {
        return ResponseCookie
                .from(SESSION_COOKIE_NAME, sessionId)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(sessionProperties.getTtlMinutes()))
                .sameSite("Lax")
                .build();
    }
}
