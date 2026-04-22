package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.entity.PatientTestId;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.exception.PatientTestMappingExistsException;
import com.clinic.diagnostic.repository.DiagnosticTestRepository;
import com.clinic.diagnostic.repository.PatientTestRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PatientTestServiceImpl implements PatientTestService {

    private PatientTestRepository patientTestRepository;
    private DiagnosticTestRepository testRepository;

    public PatientTestServiceImpl(PatientTestRepository patientTestRepository,
                                  DiagnosticTestRepository testRepository) {
        this.patientTestRepository = patientTestRepository;
        this.testRepository = testRepository;
    }

    public PatientTest assignTestToPatient(Integer patientId, Integer testId) {

        // ensure test exists (patient existence is FK enforced in DB)
        testRepository.findById(testId)
                .orElseThrow(() -> new DiagnosticTestNotFoundException("Test with Id " + testId + " not found"));

        if (patientTestRepository.existsByPatientIdAndTestId(patientId, testId)) {
            throw new PatientTestMappingExistsException("Test already assigned to patient");
        }

        PatientTest mapping = new PatientTest();
        mapping.setPatientId(patientId);
        mapping.setTestId(testId);

        return patientTestRepository.save(mapping);
    }

    public void removeTestFromPatient(Integer patientId, Integer testId) {
        patientTestRepository.deleteById(new PatientTestId(patientId, testId));
    }

    public List<DiagnosticTest> getTestsForPatient(Integer patientId) {
        List<PatientTest> mappings = patientTestRepository.findByPatientId(patientId);

        List<Integer> testIds = new ArrayList<>();
        for (PatientTest m : mappings) {
            testIds.add(m.getTestId());
        }

        return testRepository.findAllById(testIds);
    }
}
