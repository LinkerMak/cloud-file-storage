package com.linkermak.cloud_file_storage.config.security;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import com.linkermak.cloud_file_storage.filters.SessionAuthenticationFilter;
import com.linkermak.cloud_file_storage.repositories.session.SessionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SessionAuthenticationFilter redisSessionAuthenticationFilter,
                                                   AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        configureStatelessSecurity(http);
        configureSessionFilter(http, redisSessionAuthenticationFilter);

        configureAuthorization(http);
        configureAuthenticationEntryPoint(http, authenticationEntryPoint);

        disableUnusedDefaults(http);

        return http.build();
    }

    private void configureStatelessSecurity(HttpSecurity http) {
        http
                .securityContext(sc -> sc
                        .securityContextRepository(new NullSecurityContextRepository())
                )
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );
    }

    private void configureSessionFilter(HttpSecurity http, SessionAuthenticationFilter sessionAuthenticationFilter) {
        http
                .addFilterAfter(
                        sessionAuthenticationFilter,
                        SecurityContextHolderFilter.class);
    }

    private void configureAuthorization(HttpSecurity http) {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/config.js",
                                "/assets/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/api/auth/sign-in", "/api/auth/sign-up").permitAll()
                        .anyRequest().authenticated()
                );
    }

    private void configureAuthenticationEntryPoint(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) {
        http
                .exceptionHandling(e ->
                        e.authenticationEntryPoint(authenticationEntryPoint));
    }

    private void disableUnusedDefaults(HttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());
    }

    @Bean
    SessionAuthenticationFilter sessionAuthenticationFilter(
            SessionRepository sessionRepository,
            SessionProperties sessionProperties
    ) {
        return new SessionAuthenticationFilter(
                sessionRepository,
                sessionProperties);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
