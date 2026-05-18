package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.exception.DiagnosticTestExistsException;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.repository.DiagnosticTestRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiagnosticTestServiceImpl implements DiagnosticTestService {

    private DiagnosticTestRepository testRepository;

    public DiagnosticTestServiceImpl(DiagnosticTestRepository testRepository) {
        this.testRepository = testRepository;
    }

    public List<DiagnosticTest> getAllTests() {
        return testRepository.findAll();
    }

    public DiagnosticTest getTest(Integer id) {
        return testRepository.findById(id)
                .orElseThrow(() -> new DiagnosticTestNotFoundException("Test with Id " + id + " not found"));
    }

    public DiagnosticTest addTest(DiagnosticTest test) {
        if (testRepository.findByTestName(test.getTestName()).isPresent()) {
            throw new DiagnosticTestExistsException("Test name already exists");
        }
        return testRepository.save(test);
    }

    public DiagnosticTest updateTest(DiagnosticTest test) {

        DiagnosticTest existing = testRepository.findById(test.getTestId())
                .orElseThrow(() -> new DiagnosticTestNotFoundException("Test not found"));

        testRepository.findByTestName(test.getTestName())
                .filter(t -> !t.getTestId().equals(test.getTestId()))
                .ifPresent(t -> {
                    throw new DiagnosticTestExistsException("Test name already exists");
                });

        existing.setTestName(test.getTestName());
        existing.setDescription(test.getDescription());
        existing.setCost(test.getCost());

        return testRepository.save(existing);
    }

    public void deleteTest(Integer id) {
        if (!testRepository.existsById(id)) {
            throw new DiagnosticTestNotFoundException("Test with Id " + id + " not found");
        }
        testRepository.deleteById(id);
    }
}