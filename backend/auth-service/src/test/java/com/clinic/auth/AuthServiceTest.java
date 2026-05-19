package com.clinic.auth.service;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
import com.clinic.auth.repository.UserRepository;
import com.clinic.auth.security.JwtUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // set @Value("${app.internal-key}") manually without Spring context
        setPrivateField(authService, "appInternalKey", "INTERNAL_SECRET");
    }

    // ----------------------------------------------------------------------
    // ✅ LOGIN
    // ----------------------------------------------------------------------

    @Test
    void login_userNotFound_shouldThrow401() {
        LoginRequest req = mock(LoginRequest.class);

        // ONLY stub what will be used before exception
        when(req.getEmail()).thenReturn("  TEST@EMAIL.COM ");

        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid credentials", ex.getReason());

        verify(userRepository).findByEmail("test@email.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_pendingUser_shouldThrow401PendingMessage() {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn("staff@clinic.com"); // only email used

        User user = new User();
        user.setEmail("staff@clinic.com");
        user.setPassword("HASH");
        user.setRole(Role.STAFF);
        user.setStatus(AccountStatus.PENDING);

        when(userRepository.findByEmail("staff@clinic.com")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Account pending admin approval", ex.getReason());

        verify(userRepository).findByEmail("staff@clinic.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_disabledUser_shouldThrow401DisabledMessage() {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn("staff@clinic.com"); // only email used

        User user = new User();
        user.setEmail("staff@clinic.com");
        user.setPassword("HASH");
        user.setRole(Role.STAFF);
        user.setStatus(AccountStatus.DISABLED);

        when(userRepository.findByEmail("staff@clinic.com")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Account disabled. Please contact admin.", ex.getReason());

        verify(userRepository).findByEmail("staff@clinic.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_wrongPassword_shouldThrow401InvalidCredentials() {
        LoginRequest req = mock(LoginRequest.class);

        // password IS used here (because status is ACTIVE)
        when(req.getEmail()).thenReturn("user@clinic.com");
        when(req.getPassword()).thenReturn("wrong");

        User user = new User();
        user.setEmail("user@clinic.com");
        user.setPassword("HASH");
        user.setRole(Role.PATIENT);
        user.setStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail("user@clinic.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.login(req));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        assertEquals("Invalid credentials", ex.getReason());

        verify(passwordEncoder).matches("wrong", "HASH");
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_success_shouldReturnLoginResponse_andGenerateToken() {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getEmail()).thenReturn("  USER@CLINIC.COM ");
        when(req.getPassword()).thenReturn("pass");

        User user = new User();
        user.setEmail("user@clinic.com");
        user.setPassword("HASH");
        user.setRole(Role.PATIENT);
        user.setStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail("user@clinic.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "HASH")).thenReturn(true);
        when(jwtUtil.generateToken("user@clinic.com", "PATIENT")).thenReturn("JWT_TOKEN");

        LoginResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("JWT_TOKEN", response.getToken());
        assertEquals("PATIENT", response.getRole());

        verify(jwtUtil).generateToken("user@clinic.com", "PATIENT");
    }

    // ----------------------------------------------------------------------
    // ✅ SIGNUP
    // ----------------------------------------------------------------------

    @Test
    void signup_emailAlreadyExists_shouldThrow400() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("user@clinic.com"); // only email used

        when(userRepository.existsByEmail("user@clinic.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.signup(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Email already exists", ex.getReason());

        verify(userRepository).existsByEmail("user@clinic.com");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signup_invalidRole_shouldThrow400() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("user@clinic.com");
        when(req.getRole()).thenReturn("wrong_role"); // password NOT needed

        when(userRepository.existsByEmail("user@clinic.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.signup(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().startsWith("Invalid role:"));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signup_adminSelfRegister_shouldThrow403() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("admin@clinic.com");
        when(req.getRole()).thenReturn("ADMIN"); // password NOT needed

        when(userRepository.existsByEmail("admin@clinic.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.signup(req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Cannot self-register as ADMIN", ex.getReason());

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signup_doctorSelfRegister_shouldThrow403() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("doc@clinic.com");
        when(req.getRole()).thenReturn("DOCTOR"); // password NOT needed

        when(userRepository.existsByEmail("doc@clinic.com")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.signup(req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Cannot self-register as DOCTOR", ex.getReason());

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void signup_staff_shouldSavePending_andReturnAwaitingMessage() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("  staff@clinic.com ");
        when(req.getPassword()).thenReturn("pass");
        when(req.getRole()).thenReturn("STAFF");

        when(userRepository.existsByEmail("staff@clinic.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        String msg = authService.signup(req);

        assertEquals("Signup successful. Awaiting admin approval.", msg);

        User saved = userCaptor.getValue();
        assertEquals("staff@clinic.com", saved.getEmail());
        assertEquals("ENC", saved.getPassword());
        assertEquals(Role.STAFF, saved.getRole());
        assertEquals(AccountStatus.PENDING, saved.getStatus());
    }

    @Test
    void signup_defaultRolePatient_shouldSaveActive_andReturnSuccess() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("  patient@clinic.com ");
        when(req.getPassword()).thenReturn("pass");
        when(req.getRole()).thenReturn(null); // default role PATIENT

        when(userRepository.existsByEmail("patient@clinic.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        String msg = authService.signup(req);

        assertEquals("Signup successful", msg);

        User saved = userCaptor.getValue();
        assertEquals("patient@clinic.com", saved.getEmail());
        assertEquals("ENC", saved.getPassword());
        assertEquals(Role.PATIENT, saved.getRole());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
    }

    // ----------------------------------------------------------------------
    // ✅ REGISTER DOCTOR (internal/admin)
    // ----------------------------------------------------------------------

    @Test
    void registerDoctor_wrongInternalKey_shouldThrow403() {
        SignupRequest req = mock(SignupRequest.class);
        // DO NOT stub anything here - method throws before using request

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.registerDoctor("WRONG", req));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
        assertEquals("Forbidden (internal)", ex.getReason());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerDoctor_doctorAlreadyExists_shouldThrow400() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("doc@clinic.com"); // email used
        // password NOT needed because it throws before save

        when(userRepository.existsByEmail("doc@clinic.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.registerDoctor("INTERNAL_SECRET", req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Doctor already exists", ex.getReason());

        verify(userRepository).existsByEmail("doc@clinic.com");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void registerDoctor_success_shouldSaveActiveDoctor() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getEmail()).thenReturn("  DOC@CLINIC.COM ");
        when(req.getPassword()).thenReturn("pass");

        when(userRepository.existsByEmail("doc@clinic.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        authService.registerDoctor("INTERNAL_SECRET", req);

        User saved = userCaptor.getValue();
        assertEquals("doc@clinic.com", saved.getEmail());
        assertEquals("ENC", saved.getPassword());
        assertEquals(Role.DOCTOR, saved.getRole());
        assertEquals(AccountStatus.ACTIVE, saved.getStatus());
    }

    // ----------------------------------------------------------------------
    // ✅ ADMIN: list pending staff
    // ----------------------------------------------------------------------

    @Test
    void getPendingStaff_shouldReturnPendingStaffList() {
        User u1 = new User();
        u1.setRole(Role.STAFF);
        u1.setStatus(AccountStatus.PENDING);

        when(userRepository.findAllByRoleAndStatus(Role.STAFF, AccountStatus.PENDING))
                .thenReturn(Arrays.asList(u1));

        List<User> list = authService.getPendingStaff();

        assertNotNull(list);
        assertEquals(1, list.size());
        verify(userRepository).findAllByRoleAndStatus(Role.STAFF, AccountStatus.PENDING);
    }

    // ----------------------------------------------------------------------
    // ✅ ADMIN: approve staff
    // ----------------------------------------------------------------------

    @Test
    void approveStaff_userNotFound_shouldThrow404() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.approveStaff(1));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    @Test
    void approveStaff_notStaff_shouldThrow400() {
        User user = new User();
        user.setRole(Role.PATIENT);
        user.setStatus(AccountStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.approveStaff(1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only STAFF can be approved here", ex.getReason());

        verify(userRepository, never()).save(any());
    }

    @Test
    void approveStaff_success_shouldSetActive_andReturnMessage() {
        User user = new User();
        user.setRole(Role.STAFF);
        user.setStatus(AccountStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String msg = authService.approveStaff(1);

        assertEquals("Staff approved", msg);
        assertEquals(AccountStatus.ACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    // ----------------------------------------------------------------------
    // ✅ ADMIN: reject staff
    // ----------------------------------------------------------------------

    @Test
    void rejectStaff_userNotFound_shouldThrow404() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.rejectStaff(1));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("User not found", ex.getReason());
    }

    @Test
    void rejectStaff_notStaff_shouldThrow400() {
        User user = new User();
        user.setRole(Role.PATIENT);
        user.setStatus(AccountStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> authService.rejectStaff(1));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Only STAFF can be rejected here", ex.getReason());

        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectStaff_success_shouldSetDisabled_andReturnMessage() {
        User user = new User();
        user.setRole(Role.STAFF);
        user.setStatus(AccountStatus.PENDING);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String msg = authService.rejectStaff(1);

        assertEquals("Staff rejected/disabled", msg);
        assertEquals(AccountStatus.DISABLED, user.getStatus());
        verify(userRepository).save(user);
    }

    // ----------------------------------------------------------------------
    // ✅ Helper: set private field without Spring
    // ----------------------------------------------------------------------

    private static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}