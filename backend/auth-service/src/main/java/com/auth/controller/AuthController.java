package com.auth.controller;

import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterPatientRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.dto.response.UserProfileResponse;
import com.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/patient")
    public UserProfileResponse registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
        return authService.registerPatient(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}