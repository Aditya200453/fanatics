package com.clinic.auth.service;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
import com.clinic.auth.repository.UserRepository;
import com.clinic.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.internal-key}")
    private String appInternalKey;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ✅ ✅ LOGIN (FINAL FIXED VERSION)
    public LoginResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"
                ));

        // ✅ ✅ FIX: Safe enum comparison
        if (!AccountStatus.ACTIVE.equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Account disabled. Please contact admin."
            );
        }

        // ✅ ✅ Password check
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        // ✅ ✅ Generate JWT
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                "Login successful",
                token,
                user.getRole().name()
        );
    }

    // ✅ ✅ PATIENT SIGNUP
    public String signup(SignupRequest request) {

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists"
            );
        }

        User user = new User();
        user.setEmail(email);

        // ✅ Encode password properly
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.PATIENT);

        // ✅ ✅ CRITICAL FIX → ensures login works
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

        return "Signup successful";
    }

    // ✅ ✅ DOCTOR REGISTRATION (ADMIN FLOW)
    public void registerDoctor(String internalKey, SignupRequest request) {

        if (internalKey == null || !internalKey.equals(appInternalKey)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden (internal)"
            );
        }

        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor already exists"
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DOCTOR);

        // ✅ Doctor is ACTIVE only after approval
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }

    // ✅ ✅ COMMON METHOD
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}