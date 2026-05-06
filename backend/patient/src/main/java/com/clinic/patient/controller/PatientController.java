package com.clinic.patient.controller;

import com.clinic.patient.dto.AuthRegisterRequest;
import com.clinic.patient.dto.PatientSignupRequest;
import com.clinic.patient.entity.Patient;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.repository.PatientRepository;
import com.clinic.patient.service.PatientService;
import com.clinic.patient.util.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/patient") // ✅ IMPORTANT: /patient/signup
public class PatientController {

    private final PatientService patientService;
    private final PatientRepository patientRepository;
    private final RestTemplate restTemplate; // ✅ MUST be @LoadBalanced bean

    public PatientController(PatientService patientService,
                             PatientRepository patientRepository,
                             RestTemplate restTemplate) {
        this.patientService = patientService;
        this.patientRepository = patientRepository;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupPatient(@RequestBody PatientSignupRequest request) {

        // ✅ Normalize
        String email = request.getEmail() == null ? null : request.getEmail().trim().toLowerCase();
        String phone = request.getPhone() == null ? null : request.getPhone().trim();

        // ✅ Basic null checks (avoid NPE -> 500)
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Email is required"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Password is required"));
        }
        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(new ResponseMessage("Phone is required"));
        }

        // ✅ 0) Pre-check duplicates BEFORE creating auth user
        if (patientRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage("Email already exists: " + email));
        }
        if (patientRepository.existsByPhone(phone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage("Phone already exists: " + phone));
        }

        // ✅ 1) Build request for auth-service (/auth/signup expects email+password)
        AuthRegisterRequest authRequest = new AuthRegisterRequest(email, request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthRegisterRequest> entity = new HttpEntity<>(authRequest, headers);

        // ✅ 2) Call auth-service via Eureka serviceId (requires @LoadBalanced RestTemplate bean)
        try {
            restTemplate.postForEntity(
                    "http://auth-service/auth/signup",
                    entity,
                    String.class
            );
        } catch (HttpStatusCodeException ex) {
            // ✅ auth-service returned 4xx/5xx, show exact body for debugging
            String body = ex.getResponseBodyAsString();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage("Failed to create auth user: " + body));
        } catch (ResourceAccessException ex) {
            // ✅ auth-service unreachable (down/not registered)
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ResponseMessage("Auth-service unavailable. Please try again."));
        }

        // ✅ 3) Save patient AFTER auth success
        try {
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

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseMessage("Patient registered successfully"));

        } catch (PatientExistsException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage(ex.getMessage()));
        }
    }

    // -------------------- OTHER ENDPOINTS (keep as needed) --------------------

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        System.out.println("User Email: " + email);
        System.out.println("User Role : " + role);
        return ResponseEntity.ok("Headers received");
    }

    @GetMapping("/me")
    public ResponseEntity<Patient> getMyProfile(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        String role  = request.getHeader("X-User-Role");

        if (email == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Patient patient = patientService.getLoggedInPatient(email.trim().toLowerCase());
        return ResponseEntity.ok(patient);
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Patient>> findAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> getOnePatient(@PathVariable Integer id) {
        return ResponseEntity.ok(patientService.getOnePatient(id));
    }

    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Patient> storePatient(@RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.save(patient));
    }
}