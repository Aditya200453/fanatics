package com.clinic.diagnostic;

import com.clinic.diagnostic.controller.PatientTestController;
import com.clinic.diagnostic.entity.PatientTest;
import com.clinic.diagnostic.service.PatientTestService;
import com.clinic.diagnostic.util.ResponseMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientTestControllerTest {

    @Mock
    private PatientTestService patientTestService;

    @InjectMocks
    private PatientTestController controller;

    private Integer patientId;
    private Integer testId;
    private String testDateStr;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        patientId = 101;
        testId = 11;
        testDateStr = "2026-05-15";
        testDate = LocalDate.parse(testDateStr);
    }

    // ============================================================
    // ✅ POST /diagnostic/patients/{patientId}/tests/{testId}
    // assign()
    // ============================================================

    @Test
    void testAssign_success() {
        PatientTest saved = new PatientTest();
        saved.setPatientId(patientId);
        saved.setTestId(testId);
        saved.setTestDate(testDate);
        saved.setStatus("PENDING");

        // capture LocalDate passed by controller (because it parses string)
        ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        when(patientTestService.assignTestToPatient(eq(patientId), eq(testId), any(LocalDate.class)))
                .thenReturn(saved);

        ResponseEntity<PatientTest> response = controller.assign(patientId, testId, testDateStr);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(patientId, response.getBody().getPatientId());
        assertEquals(testId, response.getBody().getTestId());
        assertEquals(testDate, response.getBody().getTestDate());
        assertEquals("PENDING", response.getBody().getStatus());

        verify(patientTestService, times(1))
                .assignTestToPatient(eq(patientId), eq(testId), dateCaptor.capture());

        assertEquals(testDate, dateCaptor.getValue()); // ✅ confirms LocalDate.parse worked
    }

    // ============================================================
    // ✅ PATCH /diagnostic/patients/{patientId}/tests/{testId}
    // updateResult()
    // ============================================================

    @Test
    void testUpdateResult_success_updateBoth() {
        PatientTest updated = new PatientTest();
        updated.setPatientId(patientId);
        updated.setTestId(testId);
        updated.setResult("POSITIVE");
        updated.setStatus("COMPLETED");

        when(patientTestService.updateResult(patientId, testId, "POSITIVE", "COMPLETED"))
                .thenReturn(updated);

        ResponseEntity<PatientTest> response =
                controller.updateResult(patientId, testId, "POSITIVE", "COMPLETED");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("POSITIVE", response.getBody().getResult());
        assertEquals("COMPLETED", response.getBody().getStatus());

        verify(patientTestService, times(1))
                .updateResult(patientId, testId, "POSITIVE", "COMPLETED");
    }

    @Test
    void testUpdateResult_success_withNulls() {
        PatientTest updated = new PatientTest();
        updated.setPatientId(patientId);
        updated.setTestId(testId);
        updated.setStatus("PENDING"); // assume unchanged

        when(patientTestService.updateResult(patientId, testId, null, null))
                .thenReturn(updated);

        ResponseEntity<PatientTest> response =
                controller.updateResult(patientId, testId, null, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(patientId, response.getBody().getPatientId());
        assertEquals(testId, response.getBody().getTestId());

        verify(patientTestService, times(1))
                .updateResult(patientId, testId, null, null);
    }

    // ============================================================
    // ✅ DELETE /diagnostic/patients/{patientId}/tests/{testId}
    // remove()
    // ============================================================

    @Test
    void testRemove_success() {
        doNothing().when(patientTestService).removeTestFromPatient(patientId, testId);

        ResponseEntity<ResponseMessage> response = controller.remove(patientId, testId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Patient test removed", response.getBody().getMessage());

        verify(patientTestService, times(1)).removeTestFromPatient(patientId, testId);
    }

    // ============================================================
    // ✅ GET /diagnostic/patients/{patientId}/tests
    // getTests()
    // ============================================================

    @Test
    void testGetTests_success() {
        PatientTest pt1 = new PatientTest();
        pt1.setPatientId(patientId);
        pt1.setTestId(11);

        PatientTest pt2 = new PatientTest();
        pt2.setPatientId(patientId);
        pt2.setTestId(12);

        List<PatientTest> list = Arrays.asList(pt1, pt2);

        when(patientTestService.getTestsForPatient(patientId)).thenReturn(list);

        ResponseEntity<List<PatientTest>> response = controller.getTests(patientId);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(patientTestService, times(1)).getTestsForPatient(patientId);
    }
}