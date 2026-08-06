package com.linkermak.cloud_file_storage.config.security;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public final class SecurityPaths {

    public static final List<String> PUBLIC_PATHS = List.of(
            "/",
            "/index.html",
            "/config.js",
            "/favicon.ico",
            "/assets/**",
            "/login",
            "/api/auth/sign-in",
            "/api/auth/sign-up",
            "/error"
    );
}