package com.clinic.diagnostic.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "diagnostic_test")
public class DiagnosticTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "test_id")
    private Integer testId;

    @Column(name = "test_name", nullable = false, unique = true)
    private String testName;

    @Column(name = "cost", nullable = false)
    private BigDecimal cost;

    public DiagnosticTest() {}

    public Integer getTestId() { return testId; }
    public void setTestId(Integer testId) { this.testId = testId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}