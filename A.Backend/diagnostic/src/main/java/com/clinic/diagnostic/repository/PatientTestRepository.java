package com.clinic.diagnostic.repository;

import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.entity.PatientTestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientTestRepository extends JpaRepository<PatientTest, PatientTestId> {

    List<PatientTest> findByPatientId(Integer patientId);

    boolean existsByPatientIdAndTestId(Integer patientId, Integer testId);
}
