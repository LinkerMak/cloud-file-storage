package com.linkermak.cloud_file_storage.services.authentication;

import com.linkermak.cloud_file_storage.dto.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.exceptions.security.UserAlreadyExistsException;
import com.linkermak.cloud_file_storage.models.User;
import com.linkermak.cloud_file_storage.repositories.authentication.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        return userRepository.save(new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())));
    }
}
