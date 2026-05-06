package com.clinic.auth.controller;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("User registered");
    }

    /**
     * ✅ INTERNAL endpoint (doctor-service/admin approval flow)
     * Protect it using a shared internal key header.
     *
     * Call example:
     * POST /auth/internal/doctor
     * Header: X-INTERNAL-KEY: <your_key>
     * Body: { "email": "...", "password": "..." }
     */
    @PostMapping("/internal/doctor")
    public ResponseEntity<String> registerDoctor(
            @RequestHeader("X-INTERNAL-KEY") String internalKey,
            @Valid @RequestBody SignupRequest request) {

        authService.registerDoctor(internalKey, request);
        return ResponseEntity.ok("Doctor auth created");
    }

}
