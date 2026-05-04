package com.clinic.patient.controller;

import com.clinic.patient.dto.AuthRegisterRequest;
import com.clinic.patient.dto.PatientSignupRequest;
import com.clinic.patient.entity.Patient;
import com.clinic.patient.service.PatientService;
import com.clinic.patient.util.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping("/signup")
    public ResponseEntity<ResponseMessage> signupPatient(
            @RequestBody PatientSignupRequest request) {

        // 1️⃣ Build request for auth-service
        AuthRegisterRequest authRequest =
                new AuthRegisterRequest(
                        request.getEmail(),
                        request.getPassword()
                );

        // 2️⃣ Force JSON (this fixes your 400)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AuthRegisterRequest> entity =
                new HttpEntity<>(authRequest, headers);

        // 3️⃣ Call auth-service
        try {
            restTemplate.postForEntity(
                    "http://auth-service/auth/signup",
                    entity,
                    Void.class
            );
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            System.out.println("Auth-service status: " + ex.getStatusCode());
            System.out.println("Auth-service body  : " + ex.getResponseBodyAsString());
            return ResponseEntity.badRequest()
                    .body(new ResponseMessage("Failed to create auth user"));
        }

        // 4️⃣ Save patient only AFTER auth success
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setDob(request.getDob());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setStatus("ACTIVE");


        patientService.save(patient);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseMessage("Patient registered successfully"));
    }
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

        // Safety check (extra, gateway already enforces role)
        if (email == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Patient patient = patientService.getLoggedInPatient(email);
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

    @PostMapping(
            path = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Patient> storePatient(@RequestBody Patient patient) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.save(patient));
    }

    @PutMapping(
            path = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Patient> updatePatient(@RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.update(patient));
    }

    @DeleteMapping(path = "/")
    public ResponseEntity<ResponseMessage> removePatient(
            @RequestParam(name = "patientId", required = true) Integer id) {
        patientService.delete(id);
        return ResponseEntity.ok(new ResponseMessage("Patient deleted"));
    }

    @PatchMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Patient> updatePatientPartially(
            @PathVariable Integer id,
            @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.partialUpdate(id, patient));
    }
}
