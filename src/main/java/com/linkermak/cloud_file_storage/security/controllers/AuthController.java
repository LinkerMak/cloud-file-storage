package com.linkermak.cloud_file_storage.security.controllers;

import com.linkermak.cloud_file_storage.security.dto.SignUpRequest;
import com.linkermak.cloud_file_storage.security.models.User;
import com.linkermak.cloud_file_storage.security.services.UserRegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRegisterService userRegisterService;

    @Autowired
    public AuthController(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody SignUpRequest request) {
        User user = userRegisterService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("username", user.getUsername()));
    }
}
