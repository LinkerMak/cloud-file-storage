package com.linkermak.cloud_file_storage.config.security;

import com.linkermak.cloud_file_storage.services.authentication.userdetails.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public Long currentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUserId();
        }

        throw new IllegalStateException("Unsupported authentication principal");
    }
}
