package com.clinic.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SignupRequest {

    @NotBlank
    private String loginIdentifier;

    @NotBlank
    private String password;

    @NotNull
    private String role; // "PATIENT" or "DOCTOR" (ADMIN not allowed from signup)

    public String getLoginIdentifier() { return loginIdentifier; }
    public void setLoginIdentifier(String loginIdentifier) { this.loginIdentifier = loginIdentifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
