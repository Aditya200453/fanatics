package com.auth.service;

import com.auth.dto.request.AdminCreateUserRequest;
import com.auth.dto.response.UserProfileResponse;
import com.auth.entity.AuthUser;
import com.auth.entity.ReferenceType;
import com.auth.entity.Role;
import com.auth.exception.BadRequestException;
import com.auth.exception.NotFoundException;
import com.auth.repository.AuthUserRepository;
import com.auth.util.NormalizationUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    private final AuthUserRepository repo;
    private final PasswordEncoder encoder;

    public AdminServiceImpl(AuthUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public UserProfileResponse createUser(AdminCreateUserRequest req) {
        String email = NormalizationUtil.normalizeEmail(req.email());

        if (!NormalizationUtil.isGmail(email)) {
            throw new BadRequestException("Email must be a valid Gmail address (ending with @gmail.com)");
        }
        if (repo.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        Role role = req.role();
        AuthUser user = new AuthUser();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setRole(role);

        // enforce strict role/reference rules
        if (role == Role.ADMIN) {
            user.setReferenceType(ReferenceType.SYSTEM);
            user.setReferenceId(null);
            user.setEnabled(true); // system-level admin is enabled by default
        } else if (role == Role.DOCTOR) {
            if (req.referenceId() == null) throw new BadRequestException("referenceId (doctorId) is required for DOCTOR");
            user.setReferenceType(ReferenceType.DOCTOR);
            user.setReferenceId(req.referenceId());
            user.setEnabled(req.enabled() != null ? req.enabled() : false);
        } else if (role == Role.PATIENT) {
            if (req.referenceId() == null) throw new BadRequestException("referenceId (patientId) is required for PATIENT");
            user.setReferenceType(ReferenceType.PATIENT);
            user.setReferenceId(req.referenceId());
            user.setEnabled(req.enabled() != null ? req.enabled() : false);
        } else {
            throw new BadRequestException("Unsupported role");
        }

        AuthUser saved = repo.save(user);
        return toProfile(saved);
    }

    @Override
    @Transactional
    public UserProfileResponse enableUser(Long authUserId) {
        AuthUser u = repo.findById(authUserId).orElseThrow(() -> new NotFoundException("User not found"));
        u.setEnabled(true);
        return toProfile(repo.save(u));
    }

    @Override
    @Transactional
    public UserProfileResponse disableUser(Long authUserId) {
        AuthUser u = repo.findById(authUserId).orElseThrow(() -> new NotFoundException("User not found"));
        u.setEnabled(false);
        return toProfile(repo.save(u));
    }

    @Override
    public List<UserProfileResponse> listAll() {
        return repo.findAll().stream().map(this::toProfile).toList();
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