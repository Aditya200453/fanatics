package com.clinic.auth;

import com.clinic.auth.controller.AuthController;
import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.PendingUser;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
import com.clinic.auth.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    // ------------------------------------------------------------
    // ✅ POST /auth/signup
    // ------------------------------------------------------------
    @Test
    void signup_shouldReturnOk_withServiceMessage() {
        SignupRequest req = mock(SignupRequest.class);

        when(authService.signup(req)).thenReturn("Signup successful");

        ResponseEntity<?> response = controller.signup(req);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Signup successful", response.getBody());

        verify(authService, times(1)).signup(req);
    }

    // ------------------------------------------------------------
    // ✅ POST /auth/login
    // ------------------------------------------------------------
    @Test
    void login_shouldReturnOk_withLoginResponse() {
        LoginRequest req = mock(LoginRequest.class);

        LoginResponse loginResponse = mock(LoginResponse.class);
        when(authService.login(req)).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.login(req);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertSame(loginResponse, response.getBody());

        verify(authService, times(1)).login(req);
    }

    // ------------------------------------------------------------
    // ✅ POST /auth/internal/doctor
    // ------------------------------------------------------------
    @Test
    void registerDoctor_shouldCallService_andReturnOkMessage() {
        String internalKey = "INTERNAL_SECRET";
        SignupRequest req = mock(SignupRequest.class);

        doNothing().when(authService).registerDoctor(internalKey, req);

        ResponseEntity<String> response = controller.registerDoctor(internalKey, req);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Doctor auth created", response.getBody());

        verify(authService, times(1)).registerDoctor(internalKey, req);
    }

    // ------------------------------------------------------------
    // ✅ GET /auth/admin/staff/pending  (role header enforced)
    // ------------------------------------------------------------
    @Test
    void pendingStaff_whenNotAdmin_shouldThrow403() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.pendingStaff("DOCTOR"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Admin only", ex.getReason());

        verifyNoInteractions(authService);
    }

    @Test
    void pendingStaff_whenAdmin_shouldReturnMappedPendingUsers() {
        // Mock 2 users from service layer
        User u1 = mock(User.class);
        when(u1.getId()).thenReturn(1);
        when(u1.getEmail()).thenReturn("staff1@clinic.com");
        when(u1.getRole()).thenReturn(Role.STAFF);
        when(u1.getStatus()).thenReturn(AccountStatus.PENDING);

        User u2 = mock(User.class);
        when(u2.getId()).thenReturn(2);
        when(u2.getEmail()).thenReturn("staff2@clinic.com");
        when(u2.getRole()).thenReturn(Role.STAFF);
        when(u2.getStatus()).thenReturn(AccountStatus.PENDING);

        when(authService.getPendingStaff()).thenReturn(Arrays.asList(u1, u2));

        ResponseEntity<List<PendingUser>> response = controller.pendingStaff("ADMIN");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // Validate mapping of first record
        PendingUser p1 = response.getBody().get(0);
        // These getters must match your PendingUser DTO.
        // If your method names differ (getUserId vs getId), adjust only here.
        assertEquals(1, p1.getId());
        assertEquals("staff1@clinic.com", p1.getEmail());
        assertEquals("STAFF", p1.getRole());
        assertEquals("PENDING", p1.getStatus());

        verify(authService, times(1)).getPendingStaff();
    }

    // ------------------------------------------------------------
    // ✅ PUT /auth/admin/staff/{id}/approve
    // ------------------------------------------------------------
    @Test
    void approveStaff_shouldReturnServiceMessage() {
        when(authService.approveStaff(10)).thenReturn("Staff approved");

        ResponseEntity<String> response = controller.approveStaff(10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Staff approved", response.getBody());

        verify(authService, times(1)).approveStaff(10);
    }

    // ------------------------------------------------------------
    // ✅ PUT /auth/admin/staff/{id}/reject
    // ------------------------------------------------------------
    @Test
    void rejectStaff_shouldReturnServiceMessage() {
        when(authService.rejectStaff(10)).thenReturn("Staff rejected/disabled");

        ResponseEntity<String> response = controller.rejectStaff(10);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Staff rejected/disabled", response.getBody());

        verify(authService, times(1)).rejectStaff(10);
    }
}