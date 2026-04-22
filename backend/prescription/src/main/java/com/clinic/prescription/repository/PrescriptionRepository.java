package com.clinic.prescription.repository;

import com.clinic.prescription.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    List<Prescription> findByAppointmentId(Integer appointmentId);

    boolean existsByAppointmentId(Integer appointmentId);
}
