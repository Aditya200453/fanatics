package com.clinic.diagnostic;

import com.clinic.diagnostic.exception.DiagnosticTestExistsException;
import com.clinic.diagnostic.exception.DiagnosticTestNotFoundException;
import com.clinic.diagnostic.exception.GlobalExceptionHandler;
import com.clinic.diagnostic.exception.PatientTestMappingExistsException;
import com.clinic.diagnostic.util.ResponseMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ✅ DiagnosticTestNotFoundException → 404
    @Test
    void testHandleTestNotFound() {
        DiagnosticTestNotFoundException ex =
                new DiagnosticTestNotFoundException("Test not found");

        ResponseEntity<ResponseMessage> response =
                handler.handleTestNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Test not found", response.getBody().getMessage());
    }

    // ✅ DiagnosticTestExistsException → 409
    @Test
    void testHandleTestExists() {
        DiagnosticTestExistsException ex =
                new DiagnosticTestExistsException("Test already exists");

        ResponseEntity<ResponseMessage> response =
                handler.handleTestExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Test already exists", response.getBody().getMessage());
    }

    // ✅ PatientTestMappingExistsException → 409
    @Test
    void testHandleMappingExists() {
        PatientTestMappingExistsException ex =
                new PatientTestMappingExistsException("Mapping exists");

        ResponseEntity<ResponseMessage> response =
                handler.handleMappingExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Mapping exists", response.getBody().getMessage());
    }

    // ✅ DataIntegrityViolationException → 400
    @Test
    void testHandleDbConstraint() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("DB error");

        ResponseEntity<ResponseMessage> response =
                handler.handleDbConstraint(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("DB constraint error (check patientId/testId exists)",
                response.getBody().getMessage());
    }

    // ✅ Generic Exception → 500
    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Some error");

        ResponseEntity<ResponseMessage> response =
                handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Server Error", response.getBody().getMessage());
    }
}