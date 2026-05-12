package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.entity.PatientTestId;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.exception.PatientTestMappingExistsException;
import com.clinic.diagnostic.repository.DiagnosticTestRepository;
import com.clinic.diagnostic.repository.PatientTestRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PatientTestServiceImpl implements PatientTestService {

    private final PatientTestRepository patientTestRepository;
    private final DiagnosticTestRepository testRepository;

    public PatientTestServiceImpl(PatientTestRepository patientTestRepository,
                                  DiagnosticTestRepository testRepository) {
        this.patientTestRepository = patientTestRepository;
        this.testRepository = testRepository;
    }

    @Override
    public PatientTest assignTestToPatient(Integer patientId, Integer testId, LocalDate testDate) {

        testRepository.findById(testId)
                .orElseThrow(() -> new DiagnosticTestNotFoundException("Test not found"));

        if (patientTestRepository.existsByPatientIdAndTestId(patientId, testId)) {
            throw new PatientTestMappingExistsException("Test already assigned to patient");
        }

        PatientTest pt = new PatientTest();
        pt.setPatientId(patientId);
        pt.setTestId(testId);
        pt.setTestDate(testDate);
        pt.setStatus("PENDING");

        return patientTestRepository.save(pt);
    }

    @Override
    public PatientTest updateResult(Integer patientId, Integer testId, String result, String status) {
        PatientTest pt = patientTestRepository
                .findById(new PatientTestId(patientId, testId))
                .orElseThrow(() -> new RuntimeException("Patient test mapping not found"));

        if (result != null) pt.setResult(result);
        if (status != null) pt.setStatus(status);

        return patientTestRepository.save(pt);
    }

    @Override
    public void removeTestFromPatient(Integer patientId, Integer testId) {
        patientTestRepository.deleteById(new PatientTestId(patientId, testId));
    }

    @Override
    public List<PatientTest> getTestsForPatient(Integer patientId) {
        return patientTestRepository.findByPatientId(patientId);
    }
}