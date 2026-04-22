package com.clinic.doctor.repository;

import com.clinic.doctor.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
}