package com.linkermak.cloud_file_storage.dto.web.authentication.signrequest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest implements SignRequest {

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";

    public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
    @Pattern(regexp = USERNAME_PATTERN, message = "Username format is invalid")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")
    @Pattern(regexp = PASSWORD_PATTERN, message = "Password format is invalid")
    private String password;
}
