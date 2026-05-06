package com.clinic.diagnostic.controller;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.service.PatientTestService;
import com.clinic.diagnostic.util.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/diagnostic")
public class PatientTestController {

    private PatientTestService patientTestService;

    public PatientTestController(PatientTestService patientTestService) {
        this.patientTestService = patientTestService;
    }

    // Assign a test to a patient
    @PostMapping("/patients/{patientId}/tests/{testId}")
    public ResponseEntity<PatientTest> assign(
            @PathVariable Integer patientId,
            @PathVariable Integer testId,
            @RequestParam String testDate) {

        return ResponseEntity.ok(
                patientTestService.assignTestToPatient(
                        patientId,
                        testId,
                        LocalDate.parse(testDate)
                )
        );
    }


    @PatchMapping("/patients/{patientId}/tests/{testId}")
    public ResponseEntity<PatientTest> updateResult(
            @PathVariable Integer patientId,
            @PathVariable Integer testId,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                patientTestService.updateResult(patientId, testId, result, status)
        );
    }



    // Remove test from patient
    @DeleteMapping("/patients/{patientId}/tests/{testId}")
    public ResponseEntity<ResponseMessage> remove(@PathVariable Integer patientId,
                                                  @PathVariable Integer testId) {
        patientTestService.removeTestFromPatient(patientId, testId);
        return ResponseEntity.ok(new ResponseMessage("Patient test removed"));
    }

    // List tests assigned to patient
    @GetMapping("/patients/{patientId}/tests")
    public ResponseEntity<List<PatientTest>> getTests(
            @PathVariable Integer patientId) {

        return ResponseEntity.ok(
                patientTestService.getTestsForPatient(patientId)
        );
    }

}