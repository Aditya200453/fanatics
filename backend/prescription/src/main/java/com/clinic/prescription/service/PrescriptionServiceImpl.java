package com.clinic.prescription.service;

import com.clinic.prescription.entity.Prescription;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private PrescriptionRepository prescriptionRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
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

        // Prevent duplicate prescription for same appointment (clarity)
        if (prescriptionRepository.existsByAppointmentId(prescription.getAppointmentId())) {
            throw new PrescriptionConflictException("Prescription already exists for appointmentId " + prescription.getAppointmentId());
        }

        return prescriptionRepository.save(prescription);
    }

    public Prescription updatePrescription(Prescription prescription) {

        if (prescription.getPrescriptionId() == null) {
            throw new PrescriptionConflictException("prescriptionId is required for update");
        }

        if (!prescriptionRepository.existsById(prescription.getPrescriptionId())) {
            throw new PrescriptionNotFoundException("Prescription with Id " + prescription.getPrescriptionId() + " not found");
        }

        if (prescription.getAppointmentId() == null) {
            throw new PrescriptionConflictException("appointmentId is required");
        }
        if (prescription.getPrescriptionDate() == null) {
            throw new PrescriptionConflictException("prescriptionDate is required");
        }

        return prescriptionRepository.save(prescription);
    }

    public void deletePrescription(Integer id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new PrescriptionNotFoundException("Prescription with Id " + id + " not found");
        }
        prescriptionRepository.deleteById(id);
    }

    public List<Prescription> getPrescriptionsByAppointment(Integer appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }
}
