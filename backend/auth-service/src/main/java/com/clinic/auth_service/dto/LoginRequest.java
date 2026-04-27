package com.clinic.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank
    private String loginIdentifier;

    @NotBlank
    private String password;

    public String getLoginIdentifier() { return loginIdentifier; }
    public void setLoginIdentifier(String loginIdentifier) { this.loginIdentifier = loginIdentifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}