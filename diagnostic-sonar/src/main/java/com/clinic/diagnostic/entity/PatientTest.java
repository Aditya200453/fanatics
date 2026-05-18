package com.clinic.diagnostic.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "patient_test")
@IdClass(PatientTestId.class)
public class PatientTest {

    @Id
    @Column(name = "patient_id")
    private Integer patientId;

    @Id
    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "result", length = 255)
    private String result;

    @Column(name = "status", length = 20)
    private String status;

    public PatientTest() {
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}