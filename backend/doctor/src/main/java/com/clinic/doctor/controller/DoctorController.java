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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/doctor") // ✅ FIXED
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;
    private final RestTemplate restTemplate;

    @Value("${app.internal-key}")
    private String internalKey; // ✅ REQUIRED for auth-service

    public DoctorController(DoctorService doctorService,
                            DoctorRepository doctorRepository,
                            RestTemplate restTemplate) {
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.restTemplate = restTemplate;
    }

    // ✅ ✅ DOCTOR SIGNUP (PENDING)
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupDoctor(@RequestBody DoctorSignupRequest request) {

        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Email is required"));
        }

        if (doctorRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doctor already exists with email: " + email));
        }

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setExperience(request.getExperience());
        doctor.setQualification(request.getQualification());
        doctor.setPhone(phone);
        doctor.setEmail(email);
        doctor.setStatus("PENDING"); // ✅ IMPORTANT

        doctorService.saveDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor application submitted for approval")
        );
    }

    // ✅ ✅ ADMIN APPROVE DOCTOR
    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<ResponseMessage> approveDoctor(@PathVariable Integer id) {

        Doctor doctor = doctorService.getDoctor(id);

        if ("ACTIVE".equalsIgnoreCase(doctor.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doctor already approved"));
        }

        // ✅ Build auth request
        AuthRegisterRequest authRequest =
                new AuthRegisterRequest(
                        doctor.getEmail(),
                        "doctor123" // default password
                );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-INTERNAL-KEY", internalKey); // ✅ CRITICAL

        HttpEntity<AuthRegisterRequest> entity =
                new HttpEntity<>(authRequest, headers);

        try {
            restTemplate.postForEntity(
                    "http://auth-service/auth/internal/doctor",
                    entity,
                    String.class
            );
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage("Auth error: " + ex.getResponseBodyAsString()));
        } catch (ResourceAccessException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ResponseMessage("Auth-service unavailable"));
        }

        // ✅ Activate doctor
        doctor.setStatus("ACTIVE");
        doctorService.updateDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor approved and login enabled")
        );
    }

    // ✅ ✅ GET PROFILE
    @GetMapping("/me")
    public ResponseEntity<Doctor> getMyProfile(HttpServletRequest request) {

        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        if (email == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"DOCTOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return ResponseEntity.ok(doctor);
    }

    // ✅ OTHER APIs
    @GetMapping("/")
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(doctorService.getDoctor(id));
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> delete(@RequestParam Integer id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/admin/doctors")
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }
}