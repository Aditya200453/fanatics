package com.clinic.patient.exception;

public class PatientExistsException extends RuntimeException {
    public PatientExistsException(String message) {
        super(message);
    }
}
