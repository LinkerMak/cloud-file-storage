package com.linkermak.cloud_file_storage.security.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class UserSession implements Serializable {

    private Long userId;
    @Getter
    private String username;
    @Getter
    private List<String> authorities;
    private Instant createdAt;
}
