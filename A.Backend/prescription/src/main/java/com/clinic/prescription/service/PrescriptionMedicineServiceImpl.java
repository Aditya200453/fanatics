package com.clinic.prescription.service;

import com.clinic.prescription.entity.PrescriptionMedicine;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.repository.PrescriptionMedicineRepository;
import com.clinic.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionMedicineServiceImpl implements PrescriptionMedicineService {

    private PrescriptionMedicineRepository medicineRepository;
    private PrescriptionRepository prescriptionRepository;

    public PrescriptionMedicineServiceImpl(PrescriptionMedicineRepository medicineRepository,
                                           PrescriptionRepository prescriptionRepository) {
        this.medicineRepository = medicineRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    public PrescriptionMedicine addMedicine(Integer prescriptionId, PrescriptionMedicine medicine) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new PrescriptionNotFoundException("Prescription with Id " + prescriptionId + " not found");
        }

        if (medicine.getMedicineName() == null || medicine.getMedicineName().trim().isEmpty()) {
            throw new PrescriptionConflictException("medicineName is required");
        }

        medicine.setPrescriptionId(prescriptionId);
        return medicineRepository.save(medicine);
    }

    public List<PrescriptionMedicine> getMedicines(Integer prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            throw new PrescriptionNotFoundException("Prescription with Id " + prescriptionId + " not found");
        }
        return medicineRepository.findByPrescriptionId(prescriptionId);
    }

    public void deleteMedicine(Integer medicineId) {
        medicineRepository.deleteById(medicineId);
    }
}