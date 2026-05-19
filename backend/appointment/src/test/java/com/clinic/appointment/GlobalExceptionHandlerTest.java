package com.clinic.appointment;

import com.clinic.appointment.exception.AppointmentConflictException;
import com.clinic.appointment.exception.AppointmentNotFoundException;
import com.clinic.appointment.exception.GlobalExceptionHandler;
import com.clinic.appointment.util.ResponseMessage;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFound_shouldReturn404_andMessage() {
        AppointmentNotFoundException ex = new AppointmentNotFoundException("Appointment not found: 10");

        ResponseEntity<ResponseMessage> resp = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Appointment not found: 10", extractMessage(resp.getBody()));
    }

    @Test
    void handleConflict_shouldReturn409_andMessage() {
        AppointmentConflictException ex = new AppointmentConflictException("Slot already booked");

        ResponseEntity<ResponseMessage> resp = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Slot already booked", extractMessage(resp.getBody()));
    }

    @Test
    void handleDbConstraint_shouldReturn400_andFixedMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("FK fail");

        ResponseEntity<ResponseMessage> resp = handler.handleDbConstraint(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(
                "DB constraint error: invalid patientId/doctorId OR slot constraint violation",
                extractMessage(resp.getBody())
        );
    }

    @Test
    void handleGeneric_shouldReturn500_andFixedMessage() {
        Exception ex = new Exception("boom");

        ResponseEntity<ResponseMessage> resp = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("Internal Server Error", extractMessage(resp.getBody()));
    }

    /**
     * Robust message extractor:
     * - Works whether ResponseMessage has getMessage(), message field, or Lombok-generated accessor.
     * - Prevents test failures due to missing getter naming differences.
     */
    private static String extractMessage(ResponseMessage body) {
        try {
            // Try common getter: getMessage()
            Method m = body.getClass().getMethod("getMessage");
            Object val = m.invoke(body);
            return val == null ? null : val.toString();
        } catch (Exception ignored) {
            // Try field: message
            try {
                Field f = body.getClass().getDeclaredField("message");
                f.setAccessible(true);
                Object val = f.get(body);
                return val == null ? null : val.toString();
            } catch (Exception ignored2) {
                // Last fallback (won't be perfect, but avoids compilation issues)
                return body.toString();
            }
        }
    }
}