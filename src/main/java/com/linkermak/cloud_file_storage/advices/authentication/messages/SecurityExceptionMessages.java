package com.linkermak.cloud_file_storage.advices.authentication.messages;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SecurityExceptionMessages {
    public static final String USER_ALREADY_EXISTS_MESSAGE = "User already exists";
    public static final String INVALID_USERNAME_OR_PASSWORD_MESSAGE = "Invalid username or password";
    public static final String VALIDATION_EXCEPTION_MESSAGE = "Validation exception";
}
