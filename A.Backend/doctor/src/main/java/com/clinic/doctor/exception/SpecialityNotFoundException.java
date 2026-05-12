package com.clinic.doctor.exception;

public class SpecialityNotFoundException extends RuntimeException {
    public SpecialityNotFoundException(String message) {
        super(message);
    }
}