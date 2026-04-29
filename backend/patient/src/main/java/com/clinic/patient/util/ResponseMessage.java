package com.clinic.patient.util;

import java.time.Instant;

public class ResponseMessage {
    private String message;
    private Instant timestamp = Instant.now();

    public ResponseMessage() {}

    public ResponseMessage(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}