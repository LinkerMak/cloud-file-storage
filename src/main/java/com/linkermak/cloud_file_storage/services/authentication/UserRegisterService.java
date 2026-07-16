package com.linkermak.cloud_file_storage.services.authentication;

import com.linkermak.cloud_file_storage.dto.authentication.signrequest.SignUpRequest;
import com.linkermak.cloud_file_storage.models.User;

public interface UserRegisterService {

    User register(SignUpRequest request);

}
