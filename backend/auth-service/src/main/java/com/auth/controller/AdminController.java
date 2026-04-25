package com.auth.controller;

import com.auth.dto.request.AdminCreateUserRequest;
import com.auth.dto.response.UserProfileResponse;
import com.auth.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users")
    public UserProfileResponse createUser(@Valid @RequestBody AdminCreateUserRequest req) {
        return adminService.createUser(req);
    }

    @PutMapping("/users/{id}/enable")
    public UserProfileResponse enable(@PathVariable Long id) {
        return adminService.enableUser(id);
    }

    @PutMapping("/users/{id}/disable")
    public UserProfileResponse disable(@PathVariable Long id) {
        return adminService.disableUser(id);
    }

    @GetMapping("/users")
    public List<UserProfileResponse> listAll() {
        return adminService.listAll();
    }
}