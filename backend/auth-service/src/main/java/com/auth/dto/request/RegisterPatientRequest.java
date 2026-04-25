package com.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPatientRequest(
        @NotBlank String email,
        @NotBlank String password,
        @NotNull Long patientId
) {}