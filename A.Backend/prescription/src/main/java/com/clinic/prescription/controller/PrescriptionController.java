package com.clinic.prescription.controller;

import com.clinic.prescription.entity.Prescription;
import com.clinic.prescription.service.PrescriptionService;
import com.clinic.prescription.util.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Prescription>> getAll() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prescription> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(prescriptionService.getPrescription(id));
    }

    @PostMapping("/")
    public ResponseEntity<Prescription> create(@RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.createPrescription(prescription));
    }

    @PutMapping("/")
    public ResponseEntity<Prescription> update(@RequestBody Prescription prescription) {
        return ResponseEntity.ok(prescriptionService.updatePrescription(prescription));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Prescription> patch(@PathVariable Integer id, @RequestBody Prescription updates) {
        return ResponseEntity.ok(prescriptionService.patchPrescription(id, updates));
    }

    @DeleteMapping("/")
    public ResponseEntity<ResponseMessage> delete(@RequestParam Integer id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(new ResponseMessage("Prescription deleted"));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<List<Prescription>> getByAppointment(@PathVariable Integer appointmentId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByAppointment(appointmentId));
    }
}