package com.linkermak.cloud_file_storage.security.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/user/me")
    public ResponseEntity<Map<String, String>> showUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println(userDetails.getUsername());
        System.out.println(userDetails.getPassword());

        return ResponseEntity
                .ok()
                .body(Map.of("username", userDetails.getUsername()));
    }
}


