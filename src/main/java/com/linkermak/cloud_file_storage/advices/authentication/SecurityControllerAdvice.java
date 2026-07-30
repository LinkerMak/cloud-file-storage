package com.linkermak.cloud_file_storage.advices.authentication;

import com.linkermak.cloud_file_storage.controllers.authentication.AuthController;
import com.linkermak.cloud_file_storage.dto.web.exception.ExceptionResponse;
import com.linkermak.cloud_file_storage.exceptions.authentication.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.linkermak.cloud_file_storage.advices.authentication.messages.SecurityExceptionMessages.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = AuthController.class)
public class SecurityControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();

        String message = fieldError != null
                ? fieldError.getDefaultMessage()
                : VALIDATION_EXCEPTION_MESSAGE;

        log.warn(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ExceptionResponse(message));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ExceptionResponse> handelUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.warn(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ExceptionResponse(USER_ALREADY_EXISTS_MESSAGE));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationException(AuthenticationException e) {
        log.warn(e.getMessage(), e);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ExceptionResponse(INVALID_USERNAME_OR_PASSWORD_MESSAGE));
    }

}
