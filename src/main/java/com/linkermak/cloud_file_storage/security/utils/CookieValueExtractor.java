package com.linkermak.cloud_file_storage.security.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public final class CookieValueExtractor {

    private CookieValueExtractor() {}

    public static Optional<String> extract(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            return Optional.empty();
        }

        return List.of(cookies).stream()
                .filter(cookie ->
                        name.equals(cookie.getName()))
                .map(cookie -> cookie.getValue())
                .findFirst();
    }
}
