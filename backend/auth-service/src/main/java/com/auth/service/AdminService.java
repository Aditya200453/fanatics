package com.auth.service;

import com.auth.dto.request.AdminCreateUserRequest;
import com.auth.dto.response.UserProfileResponse;

import java.util.List;

public interface AdminService {
    UserProfileResponse createUser(AdminCreateUserRequest req);
    UserProfileResponse enableUser(Long authUserId);
    UserProfileResponse disableUser(Long authUserId);
    List<UserProfileResponse> listAll();
}