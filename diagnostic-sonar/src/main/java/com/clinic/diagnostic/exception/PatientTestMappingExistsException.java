package com.clinic.diagnostic.exception;

public class PatientTestMappingExistsException extends RuntimeException {
    public PatientTestMappingExistsException(String message) {
        super(message);
    }
}
