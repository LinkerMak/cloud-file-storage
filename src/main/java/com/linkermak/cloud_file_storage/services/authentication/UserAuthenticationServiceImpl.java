package com.linkermak.cloud_file_storage.services.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.LoginResult;
import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignRequest;
import com.linkermak.cloud_file_storage.repositories.session.SessionRepository;
import com.linkermak.cloud_file_storage.services.authentication.userdetails.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private final SessionRepository sessionRepository;
    private final AuthenticationManager authenticationManager;

    @Override
    public LoginResult login(SignRequest request) {
        Authentication unAuth = UsernamePasswordAuthenticationToken
                .unauthenticated(request.getUsername(), request.getPassword());

        Authentication auth = authenticationManager.authenticate(unAuth);

        UserSession userSession = collectUserSession((UserDetailsImpl) auth.getPrincipal());

        String sessionId = sessionRepository.save(userSession);

        return new LoginResult(
                sessionId,
                userSession.getUsername()
        );
    }

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

    @Override
    public void deleteSession(String sessionId) {
        sessionRepository.delete(sessionId);
    }
}
