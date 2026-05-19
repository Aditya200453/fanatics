package com.clinic.auth;

import com.clinic.auth.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ✅ ----------------------------------------------------------
    // ResponseStatusException → preserve status + message
    // ------------------------------------------------------------
    @Test
    void handleResponseStatus_shouldReturnProperBody() {

        ResponseStatusException ex =
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");

        ResponseEntity<Map<String, Object>> response =
                handler.handleResponseStatus(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(401, body.get("status"));

        // ✅ FIXED LINE
        assertEquals("401 UNAUTHORIZED", body.get("error"));

        assertEquals("Invalid credentials", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    // ✅ ----------------------------------------------------------
    // MethodArgumentNotValidException → 400 + field errors
    // ------------------------------------------------------------
    @Test
    void handleValidation_shouldReturnFieldErrors() {

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "object");

        bindingResult.addError(new FieldError("object", "email", "Email is required"));
        bindingResult.addError(new FieldError("object", "password", "Password is required"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(400, body.get("status"));
        assertEquals("BAD_REQUEST", body.get("error"));
        assertEquals("Validation failed", body.get("message"));
        assertNotNull(body.get("timestamp"));

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors =
                (Map<String, String>) body.get("fieldErrors");

        assertEquals("Email is required", fieldErrors.get("email"));
        assertEquals("Password is required", fieldErrors.get("password"));
    }

    // ✅ ----------------------------------------------------------
    // Generic Exception → 500
    // ------------------------------------------------------------
    @Test
    void handleAny_shouldReturnInternalServerError() {

        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleAny(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);

        assertEquals(500, body.get("status"));
        assertEquals("INTERNAL_SERVER_ERROR", body.get("error"));
        assertEquals("Unexpected error", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }
}