package com.linkermak.cloud_file_storage.exceptions.session;

import org.springframework.security.core.AuthenticationException;

public class SessionAuthenticationException extends AuthenticationException {
    public SessionAuthenticationException(String message) {
        super(message);
    }

    public SessionAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
