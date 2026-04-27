package com.clinic.auth_service.service;

import com.clinic.auth_service.dto.LoginRequest;
import com.clinic.auth_service.dto.SignupRequest;
import com.clinic.auth_service.dto.AuthResponse;

public interface AuthService {
    AuthResponse signup(SignupRequest request);
    AuthResponse login(LoginRequest request);
}
