package com.auth.repository;

import com.auth.entity.AuthUser;
import com.auth.entity.ReferenceType;
import com.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmail(String email);
    boolean existsByEmail(String email);

    List<AuthUser> findByRole(Role role);
    List<AuthUser> findByRoleAndEnabled(Role role, boolean enabled);

    Optional<AuthUser> findByReferenceTypeAndReferenceId(ReferenceType referenceType, Long referenceId);
}