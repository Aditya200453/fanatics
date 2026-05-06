package com.clinic.prescription.service;

import com.clinic.prescription.entity.Prescription;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.repository.PrescriptionMedicineRepository;
import com.clinic.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private PrescriptionRepository prescriptionRepository;
    private PrescriptionMedicineRepository medicineRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                   PrescriptionMedicineRepository medicineRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
    }

    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    public Prescription getPrescription(Integer id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException("Prescription with Id " + id + " not found"));
    }

    public Prescription createPrescription(Prescription prescription) {
        if (prescription.getAppointmentId() == null) {
            throw new PrescriptionConflictException("appointmentId is required");
        }
        if (prescription.getPrescriptionDate() == null) {
            throw new PrescriptionConflictException("prescriptionDate is required");
        }

        // Business rule (optional): only one prescription per appointment
        if (prescriptionRepository.existsByAppointmentId(prescription.getAppointmentId())) {
            throw new PrescriptionConflictException(
                    "Prescription already exists for appointmentId " + prescription.getAppointmentId());
        }

        return prescriptionRepository.save(prescription);
    }

    public Prescription updatePrescription(Prescription prescription) {
        if (prescription.getPrescriptionId() == null) {
            throw new PrescriptionConflictException("prescriptionId is required for update");
        }
        if (!prescriptionRepository.existsById(prescription.getPrescriptionId())) {
            throw new PrescriptionNotFoundException(
                    "Prescription with Id " + prescription.getPrescriptionId() + " not found");
        }
        if (prescription.getAppointmentId() == null) {
            throw new PrescriptionConflictException("appointmentId is required");
        }
        if (prescription.getPrescriptionDate() == null) {
            throw new PrescriptionConflictException("prescriptionDate is required");
        }
        return prescriptionRepository.save(prescription);
    }

    public Prescription patchPrescription(Integer id, Prescription updates) {
        Prescription existing = getPrescription(id);

        // NOT NULL fields: cannot be patched to null
        if (updates.getAppointmentId() != null) {
            existing.setAppointmentId(updates.getAppointmentId());
        }

        if (updates.getPrescriptionDate() != null) {
            existing.setPrescriptionDate(updates.getPrescriptionDate());
        }

        // Nullable fields: allow update (including explicit null if you send it as null)
        // NOTE: If you want "null clears field" behavior, keep this as-is.
        if (updates.getDiagnosis() != null) {
            existing.setDiagnosis(updates.getDiagnosis());
        }
        if (updates.getNotes() != null) {
            existing.setNotes(updates.getNotes());
        }

        // Safety: if client tries to nullify required fields via PATCH by sending null,
        // it won't change because we only apply non-null updates above.
        return prescriptionRepository.save(existing);
    }

    @Transactional
    public void deletePrescription(Integer id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new PrescriptionNotFoundException("Prescription with Id " + id + " not found");
        }

        // Prevent FK violation: delete children first (DDL doesn't specify ON DELETE CASCADE)
        medicineRepository.deleteByPrescriptionId(id);
        prescriptionRepository.deleteById(id);
    }

    public List<Prescription> getPrescriptionsByAppointment(Integer appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }
}
