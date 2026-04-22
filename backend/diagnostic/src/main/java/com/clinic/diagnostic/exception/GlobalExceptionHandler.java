package com.clinic.diagnostic.exception;

import com.clinic.diagnostic.util.ResponseMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DiagnosticTestNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleTestNotFound(DiagnosticTestNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DiagnosticTestExistsException.class)
    public ResponseEntity<ResponseMessage> handleTestExists(DiagnosticTestExistsException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PatientTestMappingExistsException.class)
    public ResponseEntity<ResponseMessage> handleMappingExists(PatientTestMappingExistsException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseMessage> handleDbConstraint(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(new ResponseMessage("DB constraint error (check patientId/testId exists)"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGeneric(Exception ex) {
        return new ResponseEntity<>(new ResponseMessage("Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
