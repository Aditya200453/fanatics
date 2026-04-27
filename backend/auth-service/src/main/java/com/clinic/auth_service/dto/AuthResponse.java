package com.clinic.auth_service.dto;

public class AuthResponse {

    private String message;
    private Integer userId;
    private String role;
    private String status;
    private Integer referenceId;
    private String token; // null for signup if you want

    public AuthResponse() {}

    public AuthResponse(String message, Integer userId, String role, String status, Integer referenceId, String token) {
        this.message = message;
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.referenceId = referenceId;
        this.token = token;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
