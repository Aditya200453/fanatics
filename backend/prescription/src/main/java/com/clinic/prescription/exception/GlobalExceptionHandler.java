package com.clinic.prescription.exception;

import com.clinic.prescription.util.ResponseMessage;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PrescriptionNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleNotFound(PrescriptionNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PrescriptionConflictException.class)
    public ResponseEntity<ResponseMessage> handleConflict(PrescriptionConflictException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseMessage> handleDbConstraint(DataIntegrityViolationException ex) {
        // Happens if appointment_id does not exist (FK) or prescription_id not found for medicine insert (FK)
        return new ResponseEntity<>(new ResponseMessage("DB constraint error (check appointmentId / prescriptionId exists)"),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGeneric(Exception ex) {
        return new ResponseEntity<>(new ResponseMessage("Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
