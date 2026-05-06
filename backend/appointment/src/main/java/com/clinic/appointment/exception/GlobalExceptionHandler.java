package com.clinic.appointment.exception;

import com.clinic.appointment.util.ResponseMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleNotFound(AppointmentNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AppointmentConflictException.class)
    public ResponseEntity<ResponseMessage> handleConflict(AppointmentConflictException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseMessage> handleDbConstraint(DataIntegrityViolationException ex) {
        // Likely FK constraint fail: patient_id or doctor_id not found
        return new ResponseEntity<>(
                new ResponseMessage("DB constraint error: invalid patientId/doctorId OR slot constraint violation"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGeneric(Exception ex) {
        return new ResponseEntity<>(new ResponseMessage("Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
