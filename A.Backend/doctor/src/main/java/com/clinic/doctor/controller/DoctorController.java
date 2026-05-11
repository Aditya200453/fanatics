package com.clinic.doctor.controller;

import com.clinic.doctor.dto.AuthRegisterRequest;
import com.clinic.doctor.dto.DoctorSignupRequest;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.service.DoctorService;
import com.clinic.doctor.util.ResponseMessage;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final RestTemplate restTemplate;

    @Value("${app.internal-key}")
    private String internalKey;

    public DoctorController(DoctorService doctorService,
                            DoctorRepository doctorRepository,
                            RestTemplate restTemplate) {
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.restTemplate = restTemplate;
    }

    // ✅ DOCTOR SIGNUP (stores temp password until admin approves)
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupDoctor(@RequestBody DoctorSignupRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (doctorRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doctor already exists"));
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Password must be at least 6 characters"));
        }

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setEmail(email);
        doctor.setPhone(request.getPhone());
        doctor.setExperience(request.getExperience());
        doctor.setQualification(request.getQualification());

        doctor.setStatus("PENDING");
        doctor.setTempPassword(request.getPassword()); // ✅ store temporarily

        doctorService.saveDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor signup submitted ✅ (waiting for approval)")
        );
    }

    // ✅ ADMIN → list doctors for UI
    @GetMapping("/admin/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors(HttpServletRequest request) {

        String role = request.getHeader("X-User-Role");

        // ✅ ONLY ADMIN + STAFF
        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Doctor>> getAllDoctorsForStaff(HttpServletRequest request) {

        String role = request.getHeader("X-User-Role");

        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        System.out.println(role+" ");

        return ResponseEntity.ok(doctorRepository.findAll());
    }


    // ✅ ADMIN → approve doctor: creates auth user as DOCTOR using stored temp password
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<ResponseMessage> approveDoctor(@PathVariable Integer id) {

        Doctor doctor = doctorService.getDoctor(id);

        if ("ACTIVE".equalsIgnoreCase(doctor.getStatus())) {
            return ResponseEntity.ok(new ResponseMessage("Doctor already approved ✅"));
        }

        if (doctor.getTempPassword() == null || doctor.getTempPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doctor password missing. Please re-register doctor."));
        }

        AuthRegisterRequest authRequest = new AuthRegisterRequest(
                doctor.getEmail(),
                doctor.getTempPassword() // ✅ use actual password doctor entered
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-INTERNAL-KEY", internalKey);

        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(authRequest, headers);

        try {
            // ✅ Use internal endpoint so role becomes DOCTOR (not PATIENT)
            // This matches the intended “Admin approves → auth user created with DOCTOR role” flow. [1](https://insightgloballlc-my.sharepoint.com/personal/pantham_jashwanth_insightglobal_com/Documents/Microsoft%20Teams%20Chat%20Files/signups%20and%20logins.pdf?web=1)
            restTemplate.postForEntity(
                    "http://auth-service/auth/internal/doctor",
                    entity,
                    String.class
            );
        } catch (HttpStatusCodeException ex) {
            // If user already exists, ignore and continue to activate doctor
            // (prevents "Email already exists" blocking approvals)
            String body = ex.getResponseBodyAsString();
            System.out.println("Auth-service error while creating doctor auth: " + body);
        }

        // ✅ Activate doctor and clear temp password
        doctor.setStatus("ACTIVE");
        doctor.setTempPassword(null); // ✅ do not keep plaintext password
        doctorService.updateDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor approved and login enabled ✅")
        );
    }

    // ✅ doctor profile
    @GetMapping("/me")
    public ResponseEntity<Doctor> getMyProfile(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Doctor doctor = doctorRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return ResponseEntity.ok(doctor);
    }

    // ✅ INTERNAL: doctorId by email (used by appointment-service)
    @GetMapping("/internal/id")
    public ResponseEntity<Integer> getDoctorIdByEmail(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @RequestParam String email) {

        if (!internalKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Doctor doctor = doctorRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return ResponseEntity.ok(doctor.getDoctorId());
    }

    // ✅ PUBLIC: active doctors
    @GetMapping("/public/active")
    public ResponseEntity<List<Doctor>> getActiveDoctors() {
        return ResponseEntity.ok(doctorRepository.findByStatus("ACTIVE"));
    }
}