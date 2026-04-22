package com.clinic.prescription.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "prescription_medicine")
public class PrescriptionMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_medicine_id")
    private Integer prescriptionMedicineId;

    @Column(name = "prescription_id", nullable = false)
    private Integer prescriptionId;

    @Column(name = "medicine_name", nullable = false, length = 100)
    private String medicineName;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "instructions", length = 255)
    private String instructions;

    public PrescriptionMedicine() {}

    public Integer getPrescriptionMedicineId() { return prescriptionMedicineId; }
    public void setPrescriptionMedicineId(Integer prescriptionMedicineId) { this.prescriptionMedicineId = prescriptionMedicineId; }

    public Integer getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(Integer prescriptionId) { this.prescriptionId = prescriptionId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}