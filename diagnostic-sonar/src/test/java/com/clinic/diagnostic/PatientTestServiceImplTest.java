package com.clinic.diagnostic;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.entity.PatientTestId;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.exception.PatientTestMappingExistsException;
import com.clinic.diagnostic.repository.DiagnosticTestRepository;
import com.clinic.diagnostic.repository.PatientTestRepository;

import com.clinic.diagnostic.service.PatientTestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientTestServiceImplTest {

    @Mock
    private PatientTestRepository patientTestRepository;

    @Mock
    private DiagnosticTestRepository testRepository;

    @InjectMocks
    private PatientTestServiceImpl patientTestService;

    private Integer patientId;
    private Integer testId;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        patientId = 101;
        testId = 11;
        testDate = LocalDate.of(2026, 5, 15);
    }

    // ============================================================
    // ✅ assignTestToPatient()
    // ============================================================

    @Test
    void testAssignTestToPatient_success() {
        // test exists
        when(testRepository.findById(testId)).thenReturn(Optional.of(new DiagnosticTest()));

        // mapping does not exist
        when(patientTestRepository.existsByPatientIdAndTestId(patientId, testId)).thenReturn(false);

        // save returns entity
        PatientTest saved = new PatientTest();
        saved.setPatientId(patientId);
        saved.setTestId(testId);
        saved.setTestDate(testDate);
        saved.setStatus("PENDING");

        when(patientTestRepository.save(any(PatientTest.class))).thenReturn(saved);

        PatientTest result = patientTestService.assignTestToPatient(patientId, testId, testDate);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());
        assertEquals(testId, result.getTestId());
        assertEquals(testDate, result.getTestDate());
        assertEquals("PENDING", result.getStatus());

        verify(testRepository, times(1)).findById(testId);
        verify(patientTestRepository, times(1)).existsByPatientIdAndTestId(patientId, testId);
        verify(patientTestRepository, times(1)).save(any(PatientTest.class));
    }

    @Test
    void testAssignTestToPatient_testNotFound() {
        when(testRepository.findById(testId)).thenReturn(Optional.empty());

        assertThrows(DiagnosticTestNotFoundException.class,
                () -> patientTestService.assignTestToPatient(patientId, testId, testDate));

        verify(testRepository, times(1)).findById(testId);
        verify(patientTestRepository, never()).existsByPatientIdAndTestId(anyInt(), anyInt());
        verify(patientTestRepository, never()).save(any());
    }

    @Test
    void testAssignTestToPatient_mappingAlreadyExists() {
        when(testRepository.findById(testId)).thenReturn(Optional.of(new DiagnosticTest()));
        when(patientTestRepository.existsByPatientIdAndTestId(patientId, testId)).thenReturn(true);

        assertThrows(PatientTestMappingExistsException.class,
                () -> patientTestService.assignTestToPatient(patientId, testId, testDate));

        verify(testRepository, times(1)).findById(testId);
        verify(patientTestRepository, times(1)).existsByPatientIdAndTestId(patientId, testId);
        verify(patientTestRepository, never()).save(any());
    }

    // ============================================================
    // ✅ updateResult()
    // ============================================================

    @Test
    void testUpdateResult_success_updateBothResultAndStatus() {
        PatientTest existing = new PatientTest();
        existing.setPatientId(patientId);
        existing.setTestId(testId);
        existing.setResult(null);
        existing.setStatus("PENDING");

        when(patientTestRepository.findById(any(PatientTestId.class))).thenReturn(Optional.of(existing));
        when(patientTestRepository.save(existing)).thenReturn(existing);

        PatientTest updated = patientTestService.updateResult(patientId, testId, "POSITIVE", "COMPLETED");

        assertNotNull(updated);
        assertEquals("POSITIVE", updated.getResult());
        assertEquals("COMPLETED", updated.getStatus());

        verify(patientTestRepository, times(1)).findById(any(PatientTestId.class));
        verify(patientTestRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateResult_success_updateOnlyResult() {
        PatientTest existing = new PatientTest();
        existing.setPatientId(patientId);
        existing.setTestId(testId);
        existing.setResult(null);
        existing.setStatus("PENDING");

        when(patientTestRepository.findById(any(PatientTestId.class))).thenReturn(Optional.of(existing));
        when(patientTestRepository.save(existing)).thenReturn(existing);

        PatientTest updated = patientTestService.updateResult(patientId, testId, "NEGATIVE", null);

        assertNotNull(updated);
        assertEquals("NEGATIVE", updated.getResult());
        assertEquals("PENDING", updated.getStatus()); // unchanged

        verify(patientTestRepository).save(existing);
    }

    @Test
    void testUpdateResult_success_updateOnlyStatus() {
        PatientTest existing = new PatientTest();
        existing.setPatientId(patientId);
        existing.setTestId(testId);
        existing.setResult("NA");
        existing.setStatus("PENDING");

        when(patientTestRepository.findById(any(PatientTestId.class))).thenReturn(Optional.of(existing));
        when(patientTestRepository.save(existing)).thenReturn(existing);

        PatientTest updated = patientTestService.updateResult(patientId, testId, null, "IN_PROGRESS");

        assertNotNull(updated);
        assertEquals("NA", updated.getResult()); // unchanged
        assertEquals("IN_PROGRESS", updated.getStatus());

        verify(patientTestRepository).save(existing);
    }

    @Test
    void testUpdateResult_mappingNotFound() {
        when(patientTestRepository.findById(any(PatientTestId.class))).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> patientTestService.updateResult(patientId, testId, "X", "Y"));

        assertTrue(ex.getMessage().contains("Patient test mapping not found"));

        verify(patientTestRepository, times(1)).findById(any(PatientTestId.class));
        verify(patientTestRepository, never()).save(any());
    }

    // ============================================================
    // ✅ removeTestFromPatient()
    // ============================================================

    @Test
    void testRemoveTestFromPatient_success() {
        doNothing().when(patientTestRepository).deleteById(any(PatientTestId.class));

        patientTestService.removeTestFromPatient(patientId, testId);

        verify(patientTestRepository, times(1)).deleteById(any(PatientTestId.class));
    }

    // ============================================================
    // ✅ getTestsForPatient()
    // ============================================================

    @Test
    void testGetTestsForPatient_success() {
        PatientTest pt1 = new PatientTest();
        pt1.setPatientId(patientId);
        pt1.setTestId(11);

        PatientTest pt2 = new PatientTest();
        pt2.setPatientId(patientId);
        pt2.setTestId(12);

        List<PatientTest> list = Arrays.asList(pt1, pt2);

        when(patientTestRepository.findByPatientId(patientId)).thenReturn(list);

        List<PatientTest> result = patientTestService.getTestsForPatient(patientId);

        assertEquals(2, result.size());
        verify(patientTestRepository, times(1)).findByPatientId(patientId);
    }
}