package com.clinic.prescription.service;

import com.clinic.prescription.entity.Prescription;
import java.util.List;

public interface PrescriptionService {
    List<Prescription> getAllPrescriptions();
    Prescription getPrescription(Integer id);
    Prescription createPrescription(Prescription prescription);
    Prescription updatePrescription(Prescription prescription);
    Prescription patchPrescription(Integer id, Prescription updates);
    void deletePrescription(Integer id);
    List<Prescription> getPrescriptionsByAppointment(Integer appointmentId);
}