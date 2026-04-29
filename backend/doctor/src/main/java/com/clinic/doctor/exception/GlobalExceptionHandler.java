package com.clinic.doctor.exception;

import com.clinic.doctor.util.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleDoctorNotFound(DoctorNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(SpecialityNotFoundException.class)
    public ResponseEntity<ResponseMessage> handleSpecialityNotFound(SpecialityNotFoundException ex) {
        return new ResponseEntity<>(new ResponseMessage(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage> handleGeneric(Exception ex) {
        return new ResponseEntity<>(new ResponseMessage("Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DoctorExsistsException.class)
    public ResponseEntity<ResponseMessage> handleDoctorExists(DoctorExsistsException ex) {
        return new ResponseEntity<>(
                new ResponseMessage(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }
}