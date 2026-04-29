package com.clinic.patient.service;

import com.clinic.patient.entity.Patient;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.exception.PatientNotFoundException;
import com.clinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getOnePatient(Integer id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient with Id " + id + " not found"));
    }

    @Override
    public Patient save(Patient patient) {

        // If client sends ID during create, treat as invalid OR ignore
        if (patient.getPatientId() != null) {
            throw new PatientExistsException("PatientId must not be provided while creating a new patient");
        }

        // Unique validations before save (better error messages than DB crash)
        if (patientRepository.existsByPhone(patient.getPhone())) {
            throw new PatientExistsException("Phone already exists: " + patient.getPhone());
        }
        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new PatientExistsException("Email already exists: " + patient.getEmail());
        }

        // Default status
        if (patient.getStatus() == null || patient.getStatus().isBlank()) {
            patient.setStatus("ACTIVE");
        }

        return patientRepository.save(patient);
    }

    @Override
    public Patient update(Patient patient) {

        if (patient.getPatientId() == null) {
            throw new PatientNotFoundException("PatientId is required for update");
        }

        Patient existing = patientRepository.findById(patient.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        // Uniqueness checks only if phone/email changed (exclude current patient)
        if (patient.getPhone() != null &&
                patientRepository.existsByPhoneAndPatientIdNot(patient.getPhone(), existing.getPatientId())) {
            throw new PatientExistsException("Phone already exists: " + patient.getPhone());
        }
        if (patient.getEmail() != null &&
                patientRepository.existsByEmailAndPatientIdNot(patient.getEmail(), existing.getPatientId())) {
            throw new PatientExistsException("Email already exists: " + patient.getEmail());
        }

        // Full update fields (PUT = replace)
        existing.setName(patient.getName());
        existing.setAge(patient.getAge());
        existing.setDob(patient.getDob());
        existing.setGender(patient.getGender());
        existing.setPhone(patient.getPhone());
        existing.setEmail(patient.getEmail());
        existing.setAddress(patient.getAddress());
        existing.setStatus(patient.getStatus() == null ? "ACTIVE" : patient.getStatus());

        return patientRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException("Patient not found");
        }
        patientRepository.deleteById(id);
    }

    @Override
    public Patient partialUpdate(Integer id, Patient partialPatient) {

        Patient targetPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        if (partialPatient.getName() != null) targetPatient.setName(partialPatient.getName());
        if (partialPatient.getAge() != null) targetPatient.setAge(partialPatient.getAge());
        if (partialPatient.getDob() != null) targetPatient.setDob(partialPatient.getDob());
        if (partialPatient.getGender() != null) targetPatient.setGender(partialPatient.getGender());

        if (partialPatient.getPhone() != null) {
            if (patientRepository.existsByPhoneAndPatientIdNot(partialPatient.getPhone(), id)) {
                throw new PatientExistsException("Phone already exists: " + partialPatient.getPhone());
            }
            targetPatient.setPhone(partialPatient.getPhone());
        }

        if (partialPatient.getEmail() != null) {
            if (patientRepository.existsByEmailAndPatientIdNot(partialPatient.getEmail(), id)) {
                throw new PatientExistsException("Email already exists: " + partialPatient.getEmail());
            }
            targetPatient.setEmail(partialPatient.getEmail());
        }

        if (partialPatient.getAddress() != null) targetPatient.setAddress(partialPatient.getAddress());

        if (partialPatient.getStatus() != null) {
            targetPatient.setStatus(partialPatient.getStatus());
        }

        return patientRepository.save(targetPatient);
    }
}