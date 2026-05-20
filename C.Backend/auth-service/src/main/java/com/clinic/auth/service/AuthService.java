package com.clinic.auth.service;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.entity.AccountStatus;
import com.clinic.auth.entity.Role;
import com.clinic.auth.entity.User;
import com.clinic.auth.repository.UserRepository;
import com.clinic.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.internal-key}")
    private String appInternalKey;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    //  LOGIN (block if not ACTIVE)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"
                ));

        //  PENDING / DISABLED handling
        if (AccountStatus.PENDING.equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Account pending admin approval"
            );
        }
        if (!AccountStatus.ACTIVE.equals(user.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Account disabled. Please contact admin."
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid credentials"
            );
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new LoginResponse(
                "Login successful",
                token,
                user.getRole().name()
        );
    }

    //  SIGNUP (PATIENT default, STAFF -> PENDING approval)
    public String signup(SignupRequest request) {
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists"
            );
        }

        Role roleToCreate = parseRoleOrDefault(request.getRole(), Role.PATIENT);

        //  Secure: do NOT allow public creation of ADMIN / DOCTOR
        if (roleToCreate == Role.ADMIN || roleToCreate == Role.DOCTOR) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Cannot self-register as " + roleToCreate.name()
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roleToCreate);

        //  STAFF requires approval
        if (roleToCreate == Role.STAFF) {
            user.setStatus(AccountStatus.PENDING);
        } else {
            user.setStatus(AccountStatus.ACTIVE);
        }

        userRepository.save(user);

        if (roleToCreate == Role.STAFF) {
            return "Signup successful. Awaiting admin approval.";
        }
        return "Signup successful";
    }

    // DOCTOR REGISTRATION (internal/admin flow)
    public void registerDoctor(String internalKey, SignupRequest request) {
        if (internalKey == null || !internalKey.equals(appInternalKey)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden (internal)"
            );
        }

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Doctor already exists"
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DOCTOR);
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    // ADMIN: list pending STAFF
    public List<User> getPendingStaff() {
        return userRepository.findAllByRoleAndStatus(Role.STAFF, AccountStatus.PENDING);
    }

    //  ADMIN: approve STAFF
    public String approveStaff(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only STAFF can be approved here");
        }

        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        return "Staff approved";
    }

    //  ADMIN: reject/disable STAFF
    public String rejectStaff(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only STAFF can be rejected here");
        }

        user.setStatus(AccountStatus.DISABLED);
        userRepository.save(user);
        return "Staff rejected/disabled";
    }

    private Role parseRoleOrDefault(String roleStr, Role defaultRole) {
        if (roleStr == null || roleStr.trim().isEmpty()) return defaultRole;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid role: " + roleStr
            );
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
