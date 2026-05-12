package com.clinic.prescription.exception;

public class PrescriptionConflictException extends RuntimeException {
    public PrescriptionConflictException(String message) {
        super(message);
    }
}
