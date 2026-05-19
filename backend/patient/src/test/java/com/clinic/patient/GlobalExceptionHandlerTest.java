package com.clinic.patient;

import com.clinic.patient.exception.GlobalExceptionHandler;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.exception.PatientNotFoundException;
import com.clinic.patient.util.ResponseMessage;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlePatientNotFound_returns404_andMessage() {

        PatientNotFoundException ex =
                new PatientNotFoundException("Patient not found");

        ResponseEntity<ResponseMessage> response =
                handler.handlePatientNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Patient not found");
    }

    @Test
    void handlePatientExists_returns409_andMessage() {

        PatientExistsException ex =
                new PatientExistsException("Duplicate patient");

        ResponseEntity<ResponseMessage> response =
                handler.handlePatientExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Duplicate patient");
    }

    @Test
    void handleDataIntegrity_returns409_andFixedMessage() {

        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("DB error");

        ResponseEntity<ResponseMessage> response =
                handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Duplicate value violates unique constraint (email/phone)");
    }

    @Test
    void handleGenericException_returns500_andFixedMessage() {

        Exception ex = new Exception("Some unexpected error");

        ResponseEntity<ResponseMessage> response =
                handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage())
                .isEqualTo("Internal Server Error");
    }
}
