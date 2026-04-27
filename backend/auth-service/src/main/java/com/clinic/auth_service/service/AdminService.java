package com.clinic.auth_service.service;

import com.clinic.auth_service.dto.StatusUpdateRequest;

public interface AdminService {
    String updateUserStatus(StatusUpdateRequest request);
}
