package com.clinic.diagnostic.controller;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.service.DiagnosticTestService;
import com.clinic.diagnostic.util.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diagnostic")
public class DiagnosticTestController {

    private DiagnosticTestService testService;

    public DiagnosticTestController(DiagnosticTestService testService) {
        this.testService = testService;
    }

    // List all tests
    @GetMapping("/tests/")
    public ResponseEntity<List<DiagnosticTest>> getAllTests() {
        return ResponseEntity.ok(testService.getAllTests());
    }

    // Get test by id
    @GetMapping("/tests/{id}")
    public ResponseEntity<DiagnosticTest> getTest(@PathVariable Integer id) {
        return ResponseEntity.ok(testService.getTest(id));
    }

    // Add new test
    @PostMapping("/tests/")
    public ResponseEntity<DiagnosticTest> addTest(@RequestBody DiagnosticTest test) {
        return ResponseEntity.ok(testService.addTest(test));
    }

    // Update test
    @PutMapping("/tests/")
    public ResponseEntity<DiagnosticTest> updateTest(@RequestBody DiagnosticTest test) {
        return ResponseEntity.ok(testService.updateTest(test));
    }

    // Remove test
    @DeleteMapping("/tests/")
    public ResponseEntity<ResponseMessage> deleteTest(@RequestParam Integer testId) {
        testService.deleteTest(testId);
        return ResponseEntity.ok(new ResponseMessage("Test deleted"));
    }
}
