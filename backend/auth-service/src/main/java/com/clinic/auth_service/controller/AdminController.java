package com.clinic.auth_service.controller;

import com.clinic.auth_service.dto.StatusUpdateRequest;
import com.clinic.auth_service.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/users/status")
    public ResponseEntity<String> updateUserStatus(@Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(adminService.updateUserStatus(request));
    }
}
