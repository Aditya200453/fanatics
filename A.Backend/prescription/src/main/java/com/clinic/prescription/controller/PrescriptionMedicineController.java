package com.clinic.prescription.controller;

import com.clinic.prescription.entity.PrescriptionMedicine;
import com.clinic.prescription.service.PrescriptionMedicineService;
import com.clinic.prescription.util.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescription")
public class PrescriptionMedicineController {

    private PrescriptionMedicineService medicineService;

    public PrescriptionMedicineController(PrescriptionMedicineService medicineService) {
        this.medicineService = medicineService;
    }

    // Add medicine to a prescription
    @PostMapping("/{prescriptionId}/medicine/")
    public ResponseEntity<PrescriptionMedicine> addMedicine(@PathVariable Integer prescriptionId,
                                                            @RequestBody PrescriptionMedicine medicine) {
        return ResponseEntity.ok(medicineService.addMedicine(prescriptionId, medicine));
    }

    // List medicines of a prescription
    @GetMapping("/{prescriptionId}/medicine/")
    public ResponseEntity<List<PrescriptionMedicine>> getMedicines(@PathVariable Integer prescriptionId) {
        return ResponseEntity.ok(medicineService.getMedicines(prescriptionId));
    }

    // Delete a medicine row
    @DeleteMapping("/medicine/")
    public ResponseEntity<ResponseMessage> deleteMedicine(@RequestParam Integer id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(new ResponseMessage("Medicine deleted"));
    }
}