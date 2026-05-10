package com.clinic.auth.controller;

import com.clinic.auth.dto.LoginRequest;
import com.clinic.auth.dto.LoginResponse;
import com.clinic.auth.dto.PendingUser;
import com.clinic.auth.dto.SignupRequest;
import com.clinic.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // ✅ Inject AuthService only (controller should not access DB directly)
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ✅ PUBLIC: Register PATIENT or STAFF
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    // ✅ PUBLIC: Login (only ACTIVE users allowed)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ✅ INTERNAL: Create doctor auth (called by admin/doctor-service)
    @PostMapping("/internal/doctor")
    public ResponseEntity<String> registerDoctor(
            @RequestHeader("X-INTERNAL-KEY") String internalKey,
            @Valid @RequestBody SignupRequest request) {

        authService.registerDoctor(internalKey, request);
        return ResponseEntity.ok("Doctor auth created");
    }

    // ✅ ADMIN: Get all pending staff (DTO used to avoid exposing password)
//    @GetMapping("/admin/staff/pending")
//    public ResponseEntity<List<PendingUser>> pendingStaff() {
//
//        List<PendingUser> list = authService.getPendingStaff().stream()
//                .map(u -> new PendingUser(
//                        u.getId(),
//                        u.getEmail(),
//                        u.getRole().name(),
//                        u.getStatus().name()
//                ))
//                .toList();
//
//        return ResponseEntity.ok(list);
//    }

    @GetMapping("/admin/staff/pending")
    public ResponseEntity<List<PendingUser>> pendingStaff(
            @RequestHeader("X-User-Role") String role
    ) {

        if (!role.equalsIgnoreCase("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        List<PendingUser> list = authService.getPendingStaff().stream()
                .map(u -> new PendingUser(
                        u.getId(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getStatus().name()
                ))
                .toList();

        return ResponseEntity.ok(list);
    }


    // ✅ ADMIN: Approve staff → status becomes ACTIVE
    @PutMapping("/admin/staff/{id}/approve")
    public ResponseEntity<String> approveStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.approveStaff(id));
    }

    // ✅ ADMIN: Reject staff → status becomes DISABLED (or similar)
    @PutMapping("/admin/staff/{id}/reject")
    public ResponseEntity<String> rejectStaff(@PathVariable Integer id) {
        return ResponseEntity.ok(authService.rejectStaff(id));
    }
}








//package com.clinic.auth.controller;
//
//import com.clinic.auth.dto.LoginRequest;
//import com.clinic.auth.dto.LoginResponse;
//import com.clinic.auth.dto.PendingUser;
//import com.clinic.auth.dto.SignupRequest;
//import com.clinic.auth.entity.AccountStatus;
//import com.clinic.auth.entity.Role;
//import com.clinic.auth.entity.User;
//import com.clinic.auth.repository.UserRepository;
//import com.clinic.auth.service.AuthService;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/auth")
//public class AuthController {
//
//    private final AuthService authService;
//
//    private final UserRepository userRepository;
//
//    public AuthController(AuthService authService, UserRepository userRepository) {
//        this.authService = authService;
//        this.userRepository = userRepository;
//    }
//
//    // ✅ Public signup (PATIENT or STAFF)
//    @PostMapping("/signup")
//    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
//        return ResponseEntity.ok(authService.signup(request));
//    }
//
//    // ✅ Public login (only ACTIVE users)
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
//        return ResponseEntity.ok(authService.login(request));
//    }
//
//
//    /**
//     * ✅ INTERNAL endpoint (doctor-service/admin approval flow)
//     * Protect it using a shared internal key header.
//     *
//     * Call example:
//     * POST /auth/internal/doctor
//     * Header: X-INTERNAL-KEY: <your_key>
//     * Body: { "email": "...", "password": "..." }
//     */
//    @PostMapping("/internal/doctor")
//    public ResponseEntity<String> registerDoctor(
//            @RequestHeader("X-INTERNAL-KEY") String internalKey,
//            @Valid @RequestBody SignupRequest request) {
//
//        authService.registerDoctor(internalKey, request);
//        return ResponseEntity.ok("Doctor auth created");
//    }
//
//
//    // ✅ ADMIN endpoints (we’ll secure these in SecurityConfig)
////    @GetMapping("/admin/staff/pending")
////    public ResponseEntity<List<PendingUser>> pendingStaff() {
////        List<PendingUser> list = authService.getPendingStaff().stream()
////                .map(u -> new PendingUser(u.getId(), u.getEmail(), u.getRole().name(), u.getStatus().name()))
////                .collect(Collectors.toList());
////        return ResponseEntity.ok(list);
////    }
//
////    @PutMapping("/admin/staff/{id}/approve")
////    public ResponseEntity<?> approveStaff(@PathVariable Integer id) {
////        return ResponseEntity.ok(authService.approveStaff(id));
////    }
//
//    @PutMapping("/admin/staff/{id}/reject")
//    public ResponseEntity<?> rejectStaff(@PathVariable Integer id) {
//        return ResponseEntity.ok(authService.rejectStaff(id));
//    }
//
//    @GetMapping("/admin/staff/pending")
//    public ResponseEntity<List<User>> getPendingStaff(
//            @RequestHeader("X-User-Role") String role
//    ) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
//        }
//
//        List<User> users = userRepository.findAll();
//
//        List<User> pendingStaff = users.stream()
//                .filter(u ->
//                        u.getRole() == Role.STAFF &&
//                                u.getStatus() == AccountStatus.PENDING
//                )
//                .toList();
//
//        return ResponseEntity.ok(pendingStaff);
//    }
//
//
//    @PutMapping("/admin/staff/{id}/approve")
//    public ResponseEntity<String> approveStaff(
//            @RequestHeader("X-User-Role") String role,
//            @PathVariable Integer id
//    ) {
//        if (!role.equalsIgnoreCase("ADMIN")) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
//        }
//
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
//
//        if (user.getRole() != Role.STAFF) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only STAFF can be approved");
//        }
//
//        user.setStatus(AccountStatus.ACTIVE);
//        userRepository.save(user);
//
//        return ResponseEntity.ok("Staff approved successfully");
//    }
//
//}
