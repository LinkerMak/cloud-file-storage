package com.linkermak.cloud_file_storage.dto.web.authentication.signrequest;

import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.messages.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignInRequest implements SignRequest {

    @NotBlank(message = ValidationMessages.USERNAME_REQUIRED)
    @Size(min = 5, max = 20, message = ValidationMessages.USERNAME_SIZE)
    private String username;

    @NotBlank(message = ValidationMessages.PASSWORD_REQUIRED)
    @Size(min = 5, max = 20, message = ValidationMessages.PASSWORD_SIZE)
    private String password;
}
