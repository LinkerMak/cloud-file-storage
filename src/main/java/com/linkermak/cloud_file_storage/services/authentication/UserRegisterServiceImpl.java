package com.linkermak.cloud_file_storage.services.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.exceptions.authentication.UserAlreadyExistsException;
import com.linkermak.cloud_file_storage.models.User;
import com.linkermak.cloud_file_storage.repositories.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisterServiceImpl implements UserRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(SignUpRequest request) {
        log.info("Register attempt started: username={}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        User user = userRepository.save(new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())
        ));

        log.info("Register successful: userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }
}
