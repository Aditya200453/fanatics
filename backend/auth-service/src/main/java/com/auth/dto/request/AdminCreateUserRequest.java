package com.auth.dto.request;

import com.auth.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminCreateUserRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotNull Role role,
        Long referenceId,
        Boolean enabled
) {}