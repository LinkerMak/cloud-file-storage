package com.linkermak.cloud_file_storage.repositories.session;

import com.linkermak.cloud_file_storage.dto.authentication.UserSession;

import java.util.Optional;

public interface SessionRepository {

    String save(UserSession session);

    Optional<UserSession> findById(String sessionUUID);

    void delete(String sessionUUID);

    void refreshTTL(String sessionUUID);

}
