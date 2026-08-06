package com.linkermak.cloud_file_storage.advices.authentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final String UNAUTHORIZED_RESPONSE_BODY = "{\"message\":\"Unauthorized\"}";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        log.warn("Unauthorized request: method = {}, uri = {}, reason = {}, message = {}",
                request.getMethod(),
                request.getRequestURI(),
                authException.getClass().getSimpleName(),
                authException.getMessage());

        String uri = request.getRequestURI();
        String accept = Optional.ofNullable(request.getHeader("Accept")).orElse("");

        boolean isApi = uri.startsWith("/api/");
        boolean expectsJson = accept.contains("application/json");

        if (isApi || expectsJson) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(CHARACTER_ENCODING);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(UNAUTHORIZED_RESPONSE_BODY);
        } else {
            response.sendRedirect("/login");
        }
    }
}
