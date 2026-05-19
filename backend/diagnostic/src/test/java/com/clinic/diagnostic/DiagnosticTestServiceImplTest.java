package com.clinic.diagnostic;

import com.clinic.diagnostic.entity.DiagnosticTest;
import com.clinic.diagnostic.exception.DiagnosticTestExistsException;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.repository.DiagnosticTestRepository;

import com.clinic.diagnostic.service.DiagnosticTestServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticTestServiceImplTest {

    @Mock
    private DiagnosticTestRepository testRepository;

    @InjectMocks
    private DiagnosticTestServiceImpl testService;

    private DiagnosticTest diagnosticTest;

    @BeforeEach
    void setUp() {
        diagnosticTest = new DiagnosticTest();
        diagnosticTest.setTestId(1);
        diagnosticTest.setTestName("Blood Test");
        diagnosticTest.setDescription("Basic blood test");
        diagnosticTest.setCost(BigDecimal.valueOf(500.0));
    }

    // ✅ 1. getAllTests()

    @Test
    void testGetAllTests_success() {
        List<DiagnosticTest> list = Arrays.asList(diagnosticTest);

        when(testRepository.findAll()).thenReturn(list);

        List<DiagnosticTest> result = testService.getAllTests();

        assertEquals(1, result.size());
        verify(testRepository, times(1)).findAll();
    }

    // ✅ 2. getTest()

    @Test
    void testGetTest_success() {
        when(testRepository.findById(1)).thenReturn(Optional.of(diagnosticTest));

        DiagnosticTest result = testService.getTest(1);

        assertEquals("Blood Test", result.getTestName());
        verify(testRepository).findById(1);
    }

    @Test
    void testGetTest_notFound() {
        when(testRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(DiagnosticTestNotFoundException.class,
                () -> testService.getTest(1));
    }

    // ✅ 3. addTest()

    @Test
    void testAddTest_success() {
        when(testRepository.findByTestName("Blood Test"))
                .thenReturn(Optional.empty());

        when(testRepository.save(diagnosticTest))
                .thenReturn(diagnosticTest);

        DiagnosticTest result = testService.addTest(diagnosticTest);

        assertNotNull(result);
        verify(testRepository).save(diagnosticTest);
    }

    @Test
    void testAddTest_alreadyExists() {
        when(testRepository.findByTestName("Blood Test"))
                .thenReturn(Optional.of(diagnosticTest));

        assertThrows(DiagnosticTestExistsException.class,
                () -> testService.addTest(diagnosticTest));
    }

    // ✅ 4. updateTest()

    @Test
    void testUpdateTest_success() {
        DiagnosticTest existing = new DiagnosticTest();
        existing.setTestId(1);

        when(testRepository.findById(1)).thenReturn(Optional.of(existing));
        when(testRepository.findByTestName("Blood Test")).thenReturn(Optional.empty());
        when(testRepository.save(existing)).thenReturn(existing);

        DiagnosticTest updated = testService.updateTest(diagnosticTest);

        assertEquals("Blood Test", updated.getTestName());
        verify(testRepository).save(existing);
    }

    @Test
    void testUpdateTest_notFound() {
        when(testRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(DiagnosticTestNotFoundException.class,
                () -> testService.updateTest(diagnosticTest));
    }

    @Test
    void testUpdateTest_duplicateName() {
        DiagnosticTest existing = new DiagnosticTest();
        existing.setTestId(1);

        DiagnosticTest duplicate = new DiagnosticTest();
        duplicate.setTestId(2);

        when(testRepository.findById(1)).thenReturn(Optional.of(existing));
        when(testRepository.findByTestName("Blood Test"))
                .thenReturn(Optional.of(duplicate));

        assertThrows(DiagnosticTestExistsException.class,
                () -> testService.updateTest(diagnosticTest));
    }

    // ✅ 5. deleteTest()

    @Test
    void testDeleteTest_success() {
        when(testRepository.existsById(1)).thenReturn(true);

        testService.deleteTest(1);

        verify(testRepository).deleteById(1);
    }

    @Test
    void testDeleteTest_notFound() {
        when(testRepository.existsById(1)).thenReturn(false);

        assertThrows(DiagnosticTestNotFoundException.class,
                () -> testService.deleteTest(1));
    }
}
