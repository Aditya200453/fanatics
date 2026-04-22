package com.clinic.patient.service;

import com.clinic.patient.entity.Patient;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.exception.PatientNotFoundException;
import com.clinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PatientServiceImpl implements PatientService {

    private PatientRepository patientRepository;

    // ✅ Correct constructor
    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    @Override
    public Patient getOnePatient(Integer id) {
        Optional<Patient> patientOpt = patientRepository.findById(id);
        if (patientOpt.isPresent()) {
            return patientOpt.get();
        } else {
            throw new PatientNotFoundException("Patient with Id " + id + " not found");
        }
    }

    @Override
    public Patient save(Patient patient) {
        if (patient.getPatientId() != null &&
                patientRepository.existsById(patient.getPatientId())) {
            throw new PatientExistsException("Patient already exists in database");
        }
        return patientRepository.save(patient);
    }

    @Override
    public Patient update(Patient patient) {
        if (patientRepository.existsById(patient.getPatientId())) {
            return patientRepository.save(patient);
        } else {
            throw new PatientNotFoundException("Patient not found");
        }
    }

    @Override
    public void delete(Integer id) {
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
        } else {
            throw new PatientNotFoundException("Patient not found");
        }
    }

    @Override
    public Patient partialUpdate(Integer id, Patient partialPatient) {

        Patient targetPatient = patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

        if (partialPatient.getName() != null) {
            targetPatient.setName(partialPatient.getName());
        }

        if (partialPatient.getAge() != null) {
            targetPatient.setAge(partialPatient.getAge());
        }

        if (partialPatient.getGender() != null) {
            targetPatient.setGender(partialPatient.getGender());
        }

        if (partialPatient.getPhone() != null) {
            targetPatient.setPhone(partialPatient.getPhone());
        }

        if (partialPatient.getAddress() != null) {
            targetPatient.setAddress(partialPatient.getAddress());
        }

        return patientRepository.save(targetPatient);
    }
}