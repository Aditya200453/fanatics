package com.clinic.patient.repository;

import com.clinic.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);

    boolean existsByPhoneAndPatientIdNot(String phone, Integer patientId);
    boolean existsByEmailAndPatientIdNot(String email, Integer patientId);

    Optional<Patient> findByEmail(String email);
    List<Patient> findByNameContainingIgnoreCase(String name);

}