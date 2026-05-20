package com.clinic.patient.controller;

import com.clinic.patient.dto.AuthRegisterRequest;
import com.clinic.patient.dto.PatientSignupRequest;
import com.clinic.patient.entity.Patient;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.repository.PatientRepository;
import com.clinic.patient.service.PatientService;
import com.clinic.patient.util.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Entity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate;


    @Value("${app.internal-key}")
    private String internalKeyFromYaml;

    public PatientController(PatientService patientService,
                             PatientRepository patientRepository,
                             RestTemplate restTemplate) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.restTemplate = restTemplate;
    }

    //  PATIENT SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupPatient(@RequestBody PatientSignupRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String phone = request.getPhone().trim();

        if (patientRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Email already exists"));
        }

        if (patientRepository.existsByPhone(phone)) {
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Phone already exists"));
        }

        //  create user in auth-service
        AuthRegisterRequest authRequest =
                new AuthRegisterRequest(email, request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthRegisterRequest> entity =
                new HttpEntity<>(authRequest, headers);

        restTemplate.postForEntity(
                "http://auth-service/auth/signup",
                entity,
                String.class
        );

        // save patient
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setDob(request.getDob());
        patient.setGender(request.getGender());
        patient.setPhone(phone);
        patient.setEmail(email);
        patient.setAddress(request.getAddress());
        patient.setStatus("ACTIVE");

        patientService.save(patient);

        return ResponseEntity.ok(
                new ResponseMessage("Patient registered successfully")
        );
    }

    // GET LOGGED-IN PROFILE
    @GetMapping("/me")
    public ResponseEntity<Patient> getMyProfile(HttpServletRequest request) {

        String email = request.getHeader("X-User-Email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Patient patient = patientService.getLoggedInPatient(email.trim().toLowerCase());

        return ResponseEntity.ok(patient);
    }

    //  INTERNAL API (used by appointment-service)
    @GetMapping("/internal/id")
    public ResponseEntity<Integer> getPatientIdByEmail(
            @RequestHeader("X-INTERNAL-KEY") String key,
            @RequestParam String email) {

        if (!internalKeyFromYaml.equals(key)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Patient p = patientRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        return ResponseEntity.ok(p.getPatientId());
    }

    //  GET ALL PATIENTS
    @GetMapping("/all")public ResponseEntity<List<Patient>> getAllPatientsForStaff(HttpServletRequest request) {
        String role = request.getHeader("X-User-Role");

        if (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("STAFF")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(patientService.getAllPatients());
    }


    // GET ONE PATIENT
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Integer id) {
        return ResponseEntity.ok(patientService.getOnePatient(id));
    }
}