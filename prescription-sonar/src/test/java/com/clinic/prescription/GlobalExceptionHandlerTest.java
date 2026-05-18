package com.clinic.prescription;

import com.clinic.prescription.exception.GlobalExceptionHandler;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.util.ResponseMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle PrescriptionNotFoundException")
    void testHandleNotFound() {
        PrescriptionNotFoundException ex =
                new PrescriptionNotFoundException("Prescription not found");

        ResponseEntity<ResponseMessage> response = handler.handleNotFound(ex);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Prescription not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle PrescriptionConflictException")
    void testHandleConflict() {
        PrescriptionConflictException ex =
                new PrescriptionConflictException("Conflict occurred");

        ResponseEntity<ResponseMessage> response = handler.handleConflict(ex);

        assertEquals(409, response.getStatusCode().value());
        assertEquals("Conflict occurred", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolationException")
    void testHandleDbConstraint() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("DB error");

        ResponseEntity<ResponseMessage> response = handler.handleDbConstraint(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "DB constraint error (check appointmentId / prescriptionId exists)",
                response.getBody().getMessage()
        );
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException")
    void testHandleBadJson() {
        HttpMessageNotReadableException ex;
        ex = new HttpMessageNotReadableException("Invalid JSON", (HttpInputMessage) null);

        ResponseEntity<ResponseMessage> response = handler.handleBadJson(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Invalid request body / invalid date format (use yyyy-MM-dd)",
                response.getBody().getMessage()
        );
    }

    @Test
    @DisplayName("Should handle DateTimeParseException")
    void testHandleDateParse() {
        DateTimeParseException ex =
                new DateTimeParseException("Invalid date", "2026-99-99", 0);

        ResponseEntity<ResponseMessage> response = handler.handleDateParse(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Invalid date format (use yyyy-MM-dd)",
                response.getBody().getMessage()
        );
    }

    @Test
    @DisplayName("Should handle generic exception")
    void testHandleGeneric() {
        Exception ex = new RuntimeException("error");

        ResponseEntity<ResponseMessage> response = handler.handleGeneric(ex);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal Server Error", response.getBody().getMessage());
    }
}
