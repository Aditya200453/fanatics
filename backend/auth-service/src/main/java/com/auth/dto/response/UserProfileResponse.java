package com.auth.dto.response;

public record UserProfileResponse(
        Long authUserId,
        String email,
        String role,
        String referenceType,
        Long referenceId,
        boolean enabled
) {}