package com.linkermak.cloud_file_storage.dto.web.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@Getter
public class UserSession implements Serializable {
    private Long userId;
    private String username;
    private List<String> authorities;
    private Instant createdAt;
}
