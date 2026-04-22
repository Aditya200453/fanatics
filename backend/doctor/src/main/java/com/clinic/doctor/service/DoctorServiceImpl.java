package com.clinic.doctor.service;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.exception.DoctorNotFoundException;
import com.clinic.doctor.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorServiceImpl implements DoctorService {

    private DoctorRepository doctorRepository;

    public DoctorServiceImpl(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Doctor getDoctor(Integer id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with Id " + id + " not found"));
    }

    public Doctor saveDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) {
        if (!doctorRepository.existsById(doctor.getDoctorId())) {
            throw new DoctorNotFoundException("Doctor with Id " + doctor.getDoctorId() + " not found");
        }
        return doctorRepository.save(doctor);
    }

    public void deleteDoctor(Integer id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException("Doctor with Id " + id + " not found");
        }
        doctorRepository.deleteById(id);
    }
}