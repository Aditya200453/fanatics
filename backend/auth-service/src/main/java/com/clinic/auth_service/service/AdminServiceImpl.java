package com.clinic.auth_service.service;

import com.clinic.auth_service.dto.StatusUpdateRequest;
import com.clinic.auth_service.entity.AccountStatus;
import com.clinic.auth_service.entity.AuthUser;
import com.clinic.auth_service.exception.BadRequestException;
import com.clinic.auth_service.exception.NotFoundException;
import com.clinic.auth_service.repository.AuthUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AuthUserRepository authUserRepository;

    public AdminServiceImpl(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public String updateUserStatus(StatusUpdateRequest request) {

        AuthUser user = authUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found."));

        AccountStatus newStatus;
        try {
            newStatus = AccountStatus.valueOf(request.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status. Allowed: ACTIVE, DISABLED.");
        }

        if (newStatus != AccountStatus.ACTIVE && newStatus != AccountStatus.DISABLED) {
            throw new BadRequestException("Admin can only set status to ACTIVE or DISABLED.");
        }

        user.setStatus(newStatus);
        authUserRepository.save(user);

        return "User status updated to " + newStatus.name();
    }
}
