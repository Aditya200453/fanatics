package com.clinic.prescription.repository;

import com.clinic.prescription.entity.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Integer> {

    List<PrescriptionMedicine> findByPrescriptionId(Integer prescriptionId);

    void deleteByPrescriptionId(Integer prescriptionId);
}