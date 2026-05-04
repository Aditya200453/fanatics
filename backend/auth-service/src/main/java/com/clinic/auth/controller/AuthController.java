package com.clinic.auth.controller;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
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

//    @PostMapping("/force-create-admin")
//    public String createAdmin() {
//        authService.forceCreateAdmin();
//        return "Admin created / updated";
//    }

    // Optional endpoint
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/internal/doctor")
    public ResponseEntity<String> registerDoctor(
            @RequestBody SignupRequest request) {

        authService.registerDoctor(request);
        return ResponseEntity.ok("Doctor auth created");
    }

}
