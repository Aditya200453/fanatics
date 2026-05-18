package com.clinic.auth.controller;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.PendingUser;
import com.clinic.auth.dto.SignupRequest;

import com.clinic.auth.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ PUBLIC: Register PATIENT or STAFF
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    // ✅ PUBLIC: Login (only ACTIVE users allowed)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ✅ INTERNAL: Create doctor auth (called by admin/doctor-service)
    @PostMapping("/internal/doctor")
    public ResponseEntity<String> registerDoctor(
            @RequestHeader("X-INTERNAL-KEY") String internalKey,
            @Valid @RequestBody SignupRequest request) {

        authService.registerDoctor(internalKey, request);
        return ResponseEntity.ok("Doctor auth created");
    }

    // ✅ ADMIN: Get all pending staff (DTO used to avoid exposing password)
    @GetMapping("/admin/staff/pending")
    public ResponseEntity<List<PendingUser>> pendingStaff(
            @RequestHeader("X-User-Role") String role
    ) {

        if (!role.equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        List<PendingUser> list = authService.getPendingStaff().stream()
                .map(u -> new PendingUser(
                        u.getId(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getStatus().name()
                ))
                .toList();

        return ResponseEntity.ok(list);
    }


    // ✅ ADMIN: Approve staff → status becomes ACTIVE
    @PutMapping("/admin/staff/{id}/approve")
    public ResponseEntity<String> approveStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.approveStaff(id));
    }

    // ✅ ADMIN: Reject staff → status becomes DISABLED (or similar)
    @PutMapping("/admin/staff/{id}/reject")
    public ResponseEntity<String> rejectStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.rejectStaff(id));
    }
}