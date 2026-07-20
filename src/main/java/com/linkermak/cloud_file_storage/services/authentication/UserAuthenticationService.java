package com.linkermak.cloud_file_storage.services.authentication;

import com.linkermak.cloud_file_storage.dto.web.authentication.LoginResult;
import com.linkermak.cloud_file_storage.dto.web.authentication.signrequest.SignRequest;

public interface UserAuthenticationService {

    LoginResult login(SignRequest request);

    void deleteSession(String sessionId);

}
