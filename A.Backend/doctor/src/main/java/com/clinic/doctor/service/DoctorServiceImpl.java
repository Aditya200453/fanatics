package com.clinic.doctor.service;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.exception.DoctorExsistsException;
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
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new DoctorExsistsException(
                    "Doctor with email " + doctor.getEmail() + " already exists"
            );
        }
        return doctorRepository.save(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) {
        Doctor existing = doctorRepository.findById(doctor.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                        "Doctor with Id " + doctor.getDoctorId() + " not found"));

        existing.setName(doctor.getName());
        existing.setExperience(doctor.getExperience());
        existing.setQualification(doctor.getQualification());
        existing.setPhone(doctor.getPhone());
        existing.setStatus(doctor.getStatus());

        return doctorRepository.save(existing);
    }


    public void deleteDoctor(Integer id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException("Doctor with Id " + id + " not found");
        }
        doctorRepository.deleteById(id);
    }
}