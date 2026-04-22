package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.entity.PatientTest;

import java.util.List;

public interface PatientTestService {
    PatientTest assignTestToPatient(Integer patientId, Integer testId);
    void removeTestFromPatient(Integer patientId, Integer testId);
    List<DiagnosticTest> getTestsForPatient(Integer patientId);
}