package com.linkermak.cloud_file_storage.repositories.session;

import com.linkermak.cloud_file_storage.dto.web.authentication.UserSession;

import java.time.Duration;
import java.util.Optional;

public interface SessionRepository {

    String save(UserSession session);

    Optional<UserSession> findById(String sessionUUID);

    void delete(String sessionUUID);

    Duration getRemainingTTL(String sessionUUID);

    void refreshTTL(String sessionUUID);

}
