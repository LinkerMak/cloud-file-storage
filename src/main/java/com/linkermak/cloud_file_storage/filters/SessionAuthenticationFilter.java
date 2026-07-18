package com.linkermak.cloud_file_storage.filters;

import com.linkermak.cloud_file_storage.config.properties.SessionProperties;
import com.linkermak.cloud_file_storage.dto.authentication.UserSession;
import com.linkermak.cloud_file_storage.exceptions.session.SessionAuthenticationException;
import com.linkermak.cloud_file_storage.models.User;
import com.linkermak.cloud_file_storage.repositories.session.SessionRepository;
import com.linkermak.cloud_file_storage.services.authentication.userdetails.UserDetailsImpl;
import com.linkermak.cloud_file_storage.utils.CookieValueExtractor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;


public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final List<String> PUBLIC_PATHS = List.of(
            "/",
            "/index.html",
            "/config.js",
            "/favicon.ico",
            "/assets/**",
            "/api/auth/sign-in",
            "/api/auth/sign-up",
            "/error"
    );

    private final SessionRepository sessionRepository;
    private final SessionProperties sessionProperties;

    public SessionAuthenticationFilter(SessionRepository sessionRepository, SessionProperties sessionProperties) {
        this.sessionRepository = sessionRepository;
        this.sessionProperties = sessionProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return PUBLIC_PATHS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Optional<String> sessionId = CookieValueExtractor.
                extract(request, sessionProperties.getSessionCookieName());

        if (sessionId.isPresent()) {
            Optional<UserSession> userSession = sessionRepository.findById(sessionId.get());

            if (userSession.isPresent()) {
                Authentication auth = buildAuthentication(userSession.get());
                SecurityContextHolder.getContext().setAuthentication(auth);
                refreshSessionTTL(sessionId.get());
            }
        }

        filterChain.doFilter(request, response);
    }

    private Authentication buildAuthentication(UserSession userSession) {
        List<GrantedAuthority> roles = userSession.getAuthorities().stream()
                .map(stringAuthority -> new SimpleGrantedAuthority(stringAuthority))
                .map(authority -> (GrantedAuthority) authority)
                .toList();

        return new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(
                        new User(userSession.getUserId(),
                                userSession.getUsername(),
                                "")
                ),
                null,
                roles
        );
    }

    private void refreshSessionTTL(String sessionUUID) {
        try {
            Duration ttl = sessionRepository.getRemainingTTL(sessionUUID);
            if (ttl.compareTo(Duration.ofMinutes(sessionProperties.getTtlRefreshThreshold())) < 0) {
                sessionRepository.refreshTTL(sessionUUID);
            }
        } catch(IllegalStateException e) {
            throw new SessionAuthenticationException(e.getMessage(), e.getCause());
        }
    }
}
