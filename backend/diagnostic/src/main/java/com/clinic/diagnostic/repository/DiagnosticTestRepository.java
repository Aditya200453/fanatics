package com.clinic.diagnostic.repository;

import com.clinic.diagnostic.entity.DiagnosticTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Integer> {
    Optional<DiagnosticTest> findByTestName(String testName);
}
