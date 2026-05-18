package com.clinic.doctor.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    // -------------------------
    // DoctorExsistsException
    // -------------------------

    @Test
    void handleDuplicate_returns400_andProperBody() {

        DoctorExsistsException ex =
                new DoctorExsistsException("Doctor already exists");

        ResponseEntity<Map<String, Object>> response =
                handler.handleDuplicate(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> body = response.getBody();

        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("BAD_REQUEST");
        assertThat(body.get("message")).isEqualTo("Doctor already exists");
    }

    // -------------------------
    // DoctorNotFoundException
    // -------------------------

    @Test
    void handleNotFound_returns404_andProperBody() {

        DoctorNotFoundException ex =
                new DoctorNotFoundException("Doctor not found");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> body = response.getBody();

        assertThat(body.get("status")).isEqualTo(404);
        assertThat(body.get("error")).isEqualTo("NOT_FOUND");
        assertThat(body.get("message")).isEqualTo("Doctor not found");
    }

    // -------------------------
    // Generic Exception
    // -------------------------

    @Test
    void handleGeneral_returns500_andProperBody() {

        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        Map<String, Object> body = response.getBody();

        assertThat(body.get("status")).isEqualTo(500);
        assertThat(body.get("error")).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(body.get("message")).isEqualTo("Unexpected error");
    }
}