package com.clinic.auth_service.service;

import com.clinic.auth_service.dto.AuthResponse;
import com.clinic.auth_service.dto.LoginRequest;
import com.clinic.auth_service.dto.SignupRequest;
import com.clinic.auth_service.entity.AccountStatus;
import com.clinic.auth_service.entity.AuthUser;
import com.clinic.auth_service.entity.Role;
import com.clinic.auth_service.exception.BadRequestException;
import com.clinic.auth_service.exception.UnauthorizedException;
import com.clinic.auth_service.repository.AuthUserRepository;
import com.clinic.auth_service.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthUserRepository authUserRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponse signup(SignupRequest request) {

        if (authUserRepository.existsByLoginIdentifier(request.getLoginIdentifier())) {
            throw new BadRequestException("Account already exists with this login identifier.");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid role. Allowed: ADMIN, DOCTOR, PATIENT.");
        }

        AuthUser user = new AuthUser();
        user.setLoginIdentifier(request.getLoginIdentifier());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        // ✅ New rule: all roles self-signup and become ACTIVE immediately
        user.setStatus(AccountStatus.ACTIVE);

        AuthUser saved = authUserRepository.save(user);

        return new AuthResponse(
                "Signup successful",
                saved.getUserId(),
                saved.getRole().name(),
                saved.getStatus().name(),
                saved.getReferenceId(),
                null
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        AuthUser user = authUserRepository.findByLoginIdentifier(request.getLoginIdentifier())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials.");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not ACTIVE. Current status: " + user.getStatus().name());
        }

        String token = jwtTokenProvider.generateToken(
                user.getUserId(),
                user.getLoginIdentifier(),
                user.getRole().name()
        );

        return new AuthResponse(
                "Login successful",
                user.getUserId(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getReferenceId(),
                token
        );
    }
}
