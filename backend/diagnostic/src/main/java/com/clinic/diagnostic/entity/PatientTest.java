package com.clinic.diagnostic.entity;

import jakarta.persistence.*;

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

    public PatientTest() {}

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }
}
