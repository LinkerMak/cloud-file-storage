package com.linkermak.cloud_file_storage.security.services;

import com.linkermak.cloud_file_storage.security.dto.SignInRequest;
import com.linkermak.cloud_file_storage.security.dto.UserSession;
import com.linkermak.cloud_file_storage.security.repositories.RedisSessionRepository;
import com.linkermak.cloud_file_storage.security.services.userdetails.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserAuthenticationService {

    private final RedisSessionRepository redisSessionRepository;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserAuthenticationService(RedisSessionRepository redisSessionRepository, AuthenticationManager authenticationManager) {
        this.redisSessionRepository = redisSessionRepository;
        this.authenticationManager = authenticationManager;
    }

    public LoginResult login(SignInRequest request) {
        Authentication unAuth = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsername(),request.getPassword());

        Authentication auth = authenticationManager.authenticate(unAuth);

        UserSession userSession = collectUserSession((UserDetailsImpl) auth.getPrincipal());

        String sessionId = redisSessionRepository.save(userSession);

        return new LoginResult(
                sessionId,
                userSession.getUsername()
        );
    }

    public record LoginResult(String sessionId, String username) {}

    private static UserSession collectUserSession(UserDetailsImpl userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        return new UserSession(
                userDetails.getUserId(),
                userDetails.getUsername(),
                roles,
                Instant.now()
        );
    }

}
