package com.clinic.prescription.service;

import com.clinic.prescription.entity.PrescriptionMedicine;

import java.util.List;

public interface PrescriptionMedicineService {

    PrescriptionMedicine addMedicine(Integer prescriptionId, PrescriptionMedicine medicine);

    List<PrescriptionMedicine> getMedicines(Integer prescriptionId);

    void deleteMedicine(Integer medicineId);
}