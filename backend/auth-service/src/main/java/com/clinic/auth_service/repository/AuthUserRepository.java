package com.clinic.auth_service.repository;

import com.clinic.auth_service.entity.AccountStatus;
import com.clinic.auth_service.entity.AuthUser;
import com.clinic.auth_service.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Integer> {

    Optional<AuthUser> findByLoginIdentifier(String loginIdentifier);

    boolean existsByLoginIdentifier(String loginIdentifier);

    List<AuthUser> findByStatus(AccountStatus status);

    List<AuthUser> findByRole(Role role);

    List<AuthUser> findByRoleAndStatus(Role role, AccountStatus status);

    List<AuthUser> findByReferenceId(Integer referenceId);
}
