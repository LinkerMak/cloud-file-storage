package com.linkermak.cloud_file_storage.controllers.user;

import com.linkermak.cloud_file_storage.dto.web.authentication.response.UsernameResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController implements UserApi {

    @Override
    @GetMapping("/user/me")
    public ResponseEntity<UsernameResponse> showUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity
                .ok()
                .body(new UsernameResponse(userDetails.getUsername()));
    }
}


