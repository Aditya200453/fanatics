package com.clinic.patient.exception;

import com.clinic.patient.util.ResponseMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ResponseMessage> handlePatientNotFound(PatientNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PatientExistsException.class)
    public ResponseEntity<ResponseMessage> handlePatientExists(PatientExistsException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.CONFLICT);
    }

    // ✅ New: handle database unique constraint errors gracefully
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseMessage> handleDataIntegrity(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(
                new ResponseMessage("Duplicate value violates unique constraint (email/phone)"),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGenericException(Exception ex) {
        return new ResponseEntity<>(new ResponseMessage("Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
