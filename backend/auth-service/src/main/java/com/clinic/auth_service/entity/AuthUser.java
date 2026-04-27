package com.clinic.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "auth_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_user_login_identifier", columnNames = "login_identifier")
        },
        indexes = {
                @Index(name = "idx_auth_user_role", columnList = "role"),
                @Index(name = "idx_auth_user_reference_id", columnList = "reference_id")
        }
)
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "login_identifier", nullable = false, length = 150)
    private String loginIdentifier;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Force Hibernate to expect VARCHAR (NOT DB ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, columnDefinition = "varchar(20)")
    private Role role;

    @Column(name = "reference_id")
    private Integer referenceId;

    // Force Hibernate to expect VARCHAR (NOT DB ENUM)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20)")
    private AccountStatus status = AccountStatus.ACTIVE;

    // DB-managed timestamps
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = AccountStatus.ACTIVE;
    }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getLoginIdentifier() { return loginIdentifier; }
    public void setLoginIdentifier(String loginIdentifier) { this.loginIdentifier = loginIdentifier; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
