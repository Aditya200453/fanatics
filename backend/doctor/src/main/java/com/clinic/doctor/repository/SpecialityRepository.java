package com.clinic.doctor.repository;

import com.clinic.doctor.entity.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialityRepository extends JpaRepository<Speciality, Integer> {
}
