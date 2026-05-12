package com.clinic.patient.service;

import com.clinic.patient.entity.Patient;

import java.util.List;

public interface PatientService {

    List<Patient> getAllPatients();

    Patient getOnePatient(Integer id);

    Patient save(Patient patient);

    Patient update(Patient patient);

    void delete(Integer id);

    Patient partialUpdate(Integer id, Patient patient);

    Patient getLoggedInPatient(String email);
}
