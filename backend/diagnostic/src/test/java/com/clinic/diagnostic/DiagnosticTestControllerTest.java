package com.clinic.diagnostic;

import com.clinic.diagnostic.controller.DiagnosticTestController;
import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.service.DiagnosticTestService;
import com.clinic.diagnostic.util.ResponseMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticTestControllerTest {

    @Mock
    private DiagnosticTestService testService;

    @InjectMocks
    private DiagnosticTestController controller;

    private DiagnosticTest test;

    @BeforeEach
    void setUp() {
        test = new DiagnosticTest();
        test.setTestId(1);
        test.setTestName("Blood Test");
        test.setDescription("Basic blood test");
        test.setCost(BigDecimal.valueOf(500.0));
    }

    // ============================================================
    // ✅ GET /diagnostic/tests/
    // getAllTests()
    // ============================================================

    @Test
    void testGetAllTests_success() {
        List<DiagnosticTest> list = Arrays.asList(test);

        when(testService.getAllTests()).thenReturn(list);

        ResponseEntity<List<DiagnosticTest>> response = controller.getAllTests();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Blood Test", response.getBody().get(0).getTestName());

        verify(testService, times(1)).getAllTests();
    }

    // ============================================================
    // ✅ GET /diagnostic/tests/{id}
    // getTest()
    // ============================================================

    @Test
    void testGetTest_success() {
        when(testService.getTest(1)).thenReturn(test);

        ResponseEntity<DiagnosticTest> response = controller.getTest(1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTestId());
        assertEquals("Blood Test", response.getBody().getTestName());

        verify(testService, times(1)).getTest(1);
    }

    // ============================================================
    // ✅ POST /diagnostic/tests/
    // addTest()
    // ============================================================

    @Test
    void testAddTest_success() {
        when(testService.addTest(test)).thenReturn(test);

        ResponseEntity<DiagnosticTest> response = controller.addTest(test);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Blood Test", response.getBody().getTestName());

        verify(testService, times(1)).addTest(test);
    }

    // ============================================================
    // ✅ PUT /diagnostic/tests/
    // updateTest()
    // ============================================================

    @Test
    void testUpdateTest_success() {
        DiagnosticTest updated = new DiagnosticTest();
        updated.setTestId(1);
        updated.setTestName("Blood Test Updated");
        updated.setDescription("Updated desc");
        updated.setCost(BigDecimal.valueOf(650.0));

        when(testService.updateTest(test)).thenReturn(updated);

        ResponseEntity<DiagnosticTest> response = controller.updateTest(test);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Blood Test Updated", response.getBody().getTestName());

// ✅ FIXED LINE
        assertEquals(0, BigDecimal.valueOf(650.0)
                .compareTo(response.getBody().getCost()));

        verify(testService, times(1)).updateTest(test);
    }

    // ============================================================
    // ✅ DELETE /diagnostic/tests/?testId=1
    // deleteTest()
    // ============================================================

    @Test
    void testDeleteTest_success() {
        doNothing().when(testService).deleteTest(1);

        ResponseEntity<ResponseMessage> response = controller.deleteTest(1);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Test deleted", response.getBody().getMessage());

        verify(testService, times(1)).deleteTest(1);
    }
}