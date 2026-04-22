package com.clinic.diagnostic.entity;

import java.io.Serializable;
import java.util.Objects;

public class PatientTestId implements Serializable {

    private Integer patientId;
    private Integer testId;

    public PatientTestId() {}

    public PatientTestId(Integer patientId, Integer testId) {
        this.patientId = patientId;
        this.testId = testId;
    }

    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }

    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PatientTestId)) return false;
        PatientTestId that = (PatientTestId) o;
        return Objects.equals(patientId, that.patientId) &&
                Objects.equals(testId, that.testId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientId, testId);
    }
}
