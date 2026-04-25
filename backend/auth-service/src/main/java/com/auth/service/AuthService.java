package com.auth.service;

import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterPatientRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.dto.response.UserProfileResponse;

public interface AuthService {
    UserProfileResponse registerPatient(RegisterPatientRequest req);
    AuthResponse login(LoginRequest req);
    UserProfileResponse me(String email);
}