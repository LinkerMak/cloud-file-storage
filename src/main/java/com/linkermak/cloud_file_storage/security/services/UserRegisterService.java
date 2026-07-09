package com.linkermak.cloud_file_storage.security.services;

import com.linkermak.cloud_file_storage.security.dto.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.security.exceptions.UserAlreadyExistsException;
import com.linkermak.cloud_file_storage.security.models.User;
import com.linkermak.cloud_file_storage.security.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserRegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(SignUpRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        return userRepository.save(new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword())));
    }
}
