package com.clinic.doctor.repository;

import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.entity.SpecialityDoctorMapId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialityDoctorMapRepository
        extends JpaRepository<SpecialityDoctorMap, SpecialityDoctorMapId> {

    List<SpecialityDoctorMap> findBySpecialityId(Integer specialityId);
}
