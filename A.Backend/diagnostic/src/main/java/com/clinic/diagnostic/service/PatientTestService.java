package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.PatientTest;
import java.time.LocalDate;
import java.util.List;

public interface PatientTestService {

    PatientTest assignTestToPatient(Integer patientId, Integer testId, LocalDate testDate);

    PatientTest updateResult(Integer patientId, Integer testId, String result, String status);

    void removeTestFromPatient(Integer patientId, Integer testId);

    List<PatientTest> getTestsForPatient(Integer patientId);
}
