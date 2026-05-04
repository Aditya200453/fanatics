package com.clinic.doctor.controller;

import com.clinic.doctor.dto.AuthRegisterRequest;
import com.clinic.doctor.dto.DoctorSignupRequest;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.service.DoctorService;
import com.clinic.doctor.util.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("")
public class DoctorController {

    private DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/admin/approve/{id}")
    public ResponseEntity<ResponseMessage> approveDoctor(@PathVariable Integer id) {

        Doctor doctor = doctorService.getDoctor(id);

        if ("ACTIVE".equals(doctor.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Doctor already approved"));
        }

        // 🔐 Create auth user
        AuthRegisterRequest authRequest =
                new AuthRegisterRequest(
                        doctor.getEmail(),
                        "doctor123" // temp password
                );

        restTemplate.postForEntity(
                "http://auth-service/auth/internal/doctor",
                authRequest,
                Void.class
        );

        // ✅ Activate doctor
        doctor.setStatus("ACTIVE");
        doctorService.updateDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor approved and login enabled")
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Doctor> getMyProfile(HttpServletRequest request) {

        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        if (email == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Optional safety check
        if (!"DOCTOR".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Doctor doctor = doctorService
                .getAllDoctors()
                .stream()
                .filter(d -> email.equals(d.getEmail()))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found for email " + email)
                );

        return ResponseEntity.ok(doctor);
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupDoctor(
            @RequestBody DoctorSignupRequest request) {

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setExperience(request.getExperience());
        doctor.setQualification(request.getQualification());
        doctor.setPhone(request.getPhone());
        doctor.setEmail(request.getEmail());

        // 👇 VERY IMPORTANT
        doctor.setStatus("PENDING");

        doctorService.saveDoctor(doctor);

        return ResponseEntity.ok(
                new ResponseMessage("Doctor application submitted for approval")
        );
    }

    @GetMapping("/")
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(doctorService.getDoctor(id));
    }

    @PostMapping("/")
    public ResponseEntity<Doctor> add(@RequestBody Doctor doctor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.saveDoctor(doctor));
    }

    @PutMapping("/")
    public ResponseEntity<Doctor> update(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctor));
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> delete(@RequestParam Integer id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok().build();
    }
}