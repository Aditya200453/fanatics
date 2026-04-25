package com.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_user",
        uniqueConstraints = @UniqueConstraint(name = "uk_auth_user_email", columnNames = "email"),
        indexes = {
                @Index(name = "idx_auth_user_role_enabled", columnList = "role, enabled"),
                @Index(name = "idx_auth_user_reference", columnList = "reference_type, reference_id")
        })
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_user_id")
    private Long id;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", nullable = false, length = 20)
    private ReferenceType referenceType = ReferenceType.SYSTEM;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters/Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Instant getLastLogin() { return lastLogin; }
    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }
}