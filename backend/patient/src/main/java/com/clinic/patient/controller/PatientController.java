package com.clinic.patient.controller;

import com.clinic.patient.entity.Patient;
import com.clinic.patient.service.PatientService;
import com.clinic.patient.util.ResponseMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
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
        return ResponseEntity.ok(patientService.save(patient));
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
