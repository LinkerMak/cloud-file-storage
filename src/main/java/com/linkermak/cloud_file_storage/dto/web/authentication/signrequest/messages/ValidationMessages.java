package com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.messages;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ValidationMessages {
    public static final String USERNAME_REQUIRED = "Username is required";
    public static final String PASSWORD_REQUIRED = "Password is required";

    public static final String USERNAME_SIZE = "Username must be between 5 and 20 characters";
    public static final String PASSWORD_SIZE = "Password must be between 5 and 20 characters";

    public static final String USERNAME_INVALID_FORMAT = "Username format is invalid";
    public static final String PASSWORD_INVALID_FORMAT = "Username format is invalid";
}
