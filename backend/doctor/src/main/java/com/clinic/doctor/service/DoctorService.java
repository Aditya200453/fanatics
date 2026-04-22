package com.clinic.doctor.service;

import com.clinic.doctor.entity.Doctor;

import java.util.List;

public interface DoctorService {
    List<Doctor> getAllDoctors();
    Doctor getDoctor(Integer id);
    Doctor saveDoctor(Doctor doctor);
    Doctor updateDoctor(Doctor doctor);
    void deleteDoctor(Integer id);
}