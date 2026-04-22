package com.clinic.doctor.service;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;

import java.util.List;

public interface SpecialityService {

    Speciality addSpeciality(Speciality speciality);
    List<Speciality> getAllSpecialities();
    Speciality getSpeciality(Integer id);

    SpecialityDoctorMap mapDoctorToSpeciality(Integer specialityId, Integer doctorId);
    void removeDoctorFromSpeciality(Integer specialityId, Integer doctorId);

    List<Doctor> getDoctorsBySpeciality(Integer specialityId);
}