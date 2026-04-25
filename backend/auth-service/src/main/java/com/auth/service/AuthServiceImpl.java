package com.auth.service;

import com.auth.dto.request.LoginRequest;
import com.auth.dto.request.RegisterPatientRequest;
import com.auth.dto.response.AuthResponse;
import com.auth.dto.response.UserProfileResponse;
import com.auth.entity.AuthUser;
import com.auth.entity.ReferenceType;
import com.auth.entity.Role;
import com.auth.exception.BadRequestException;
import com.auth.exception.ForbiddenException;
import com.auth.exception.NotFoundException;
import com.auth.repository.AuthUserRepository;
import com.auth.security.jwt.JwtService;
import com.auth.util.NormalizationUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthServiceImpl(AuthUserRepository repo,
                           PasswordEncoder encoder,
                           JwtService jwtService,
                           AuthenticationManager authManager) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
    }

    @Override
    @Transactional
    public UserProfileResponse registerPatient(RegisterPatientRequest req) {
        String email = NormalizationUtil.normalizeEmail(req.email());

        if (!NormalizationUtil.isGmail(email)) {
            throw new BadRequestException("Email must be a valid Gmail address (ending with @gmail.com)");
        }
        if (repo.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        if (req.patientId() == null) {
            throw new BadRequestException("patientId (referenceId) is required");
        }

        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(Role.PATIENT);
        user.setReferenceType(ReferenceType.PATIENT);
        user.setReferenceId(req.patientId());

        // per design: admin approval required → default disabled
        user.setEnabled(false);

        AuthUser saved = repo.save(user);
        return toProfile(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest req) {
        String email = NormalizationUtil.normalizeEmail(req.email());

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        } catch (AuthenticationException ex) {
            throw new ForbiddenException("Invalid credentials or account is disabled/pending approval");
        }

        AuthUser user = repo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new ForbiddenException("Account disabled or pending admin approval");
        }

        user.setLastLogin(Instant.now());
        repo.save(user);

        String token = jwtService.generate(user);
        return new AuthResponse(token, user.getRole().name(), user.getReferenceId());
    }

    @Override
    public UserProfileResponse me(String email) {
        String normalized = NormalizationUtil.normalizeEmail(email);
        AuthUser user = repo.findByEmail(normalized)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toProfile(user);
    }

    private UserProfileResponse toProfile(AuthUser u) {
        return new UserProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getRole().name(),
                u.getReferenceType().name(),
                u.getReferenceId(),
                u.isEnabled()
        );
    }
}