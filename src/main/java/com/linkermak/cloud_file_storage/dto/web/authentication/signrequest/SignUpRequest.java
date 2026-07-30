package com.linkermak.cloud_file_storage.dto.web.authentication.signrequest;

import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.messages.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpRequest implements SignRequest {

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";

    public static final String PASSWORD_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$";

    @NotBlank(message = ValidationMessages.USERNAME_REQUIRED)
    @Size(min = 5, max = 20, message = ValidationMessages.USERNAME_SIZE)
    @Pattern(regexp = USERNAME_PATTERN, message = ValidationMessages.USERNAME_INVALID_FORMAT)
    private String username;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    @Size(min = 5, max = 20, message = ValidationMessages.PASSWORD_SIZE)
    @Pattern(regexp = PASSWORD_PATTERN, message = ValidationMessages.PASSWORD_INVALID_FORMAT)
    private String password;
}
