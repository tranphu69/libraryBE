package com.example.library.service;

import com.example.library.domain.User;

public interface MfaService {
    String initiateOtpChallenge(User user);
    User
}
