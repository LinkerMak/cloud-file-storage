package com.linkermak.cloud_file_storage.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignInRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
