package com.clinic.doctor.dto;

public class AuthRegisterRequest {
    private String email;
    private String password;

    public AuthRegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}