package com.clinic.diagnostic.service;

import com.clinic.diagnostic.entity.DiagnosticTest;

import java.util.List;

public interface DiagnosticTestService {
    List<DiagnosticTest> getAllTests();
    DiagnosticTest getTest(Integer id);
    DiagnosticTest addTest(DiagnosticTest test);
    DiagnosticTest updateTest(DiagnosticTest test);
    void deleteTest(Integer id);
}
