package com.linkermak.cloud_file_storage.security.config;

import com.linkermak.cloud_file_storage.security.filters.RedisSessionAuthenticationFilter;
import com.linkermak.cloud_file_storage.security.repositories.RedisSessionRepository;
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
                                                   RedisSessionAuthenticationFilter redisSessionAuthenticationFilter,
                                                   AuthenticationEntryPoint authenticationEntryPoint) throws Exception  {
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

    private void configureSessionFilter(HttpSecurity http, RedisSessionAuthenticationFilter redisSessionAuthenticationFilter) {
        http
                .addFilterAfter(
                        redisSessionAuthenticationFilter,
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
    RedisSessionAuthenticationFilter redisSessionAuthenticationFilter(
            RedisSessionRepository redisSessionRepository,
            SessionProperties sessionProperties
    ) {
        return new RedisSessionAuthenticationFilter(
                redisSessionRepository,
                sessionProperties);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
