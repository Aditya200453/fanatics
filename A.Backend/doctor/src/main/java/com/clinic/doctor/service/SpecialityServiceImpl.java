package com.clinic.doctor.service;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.entity.SpecialityDoctorMapId;
import com.clinic.doctor.exception.DoctorNotFoundException;
import com.clinic.doctor.exception.SpecialityNotFoundException;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.repository.SpecialityDoctorMapRepository;
import com.clinic.doctor.repository.SpecialityRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecialityServiceImpl implements SpecialityService {

    private SpecialityRepository specialityRepository;
    private DoctorRepository doctorRepository;
    private SpecialityDoctorMapRepository mapRepository;

    public SpecialityServiceImpl(SpecialityRepository specialityRepository,
                                 DoctorRepository doctorRepository,
                                 SpecialityDoctorMapRepository mapRepository) {
        this.specialityRepository = specialityRepository;
        this.doctorRepository = doctorRepository;
        this.mapRepository = mapRepository;
    }

    public Speciality addSpeciality(Speciality speciality) {
        return specialityRepository.save(speciality);
    }

    public List<Speciality> getAllSpecialities() {
        return specialityRepository.findAll();
    }

    public Speciality getSpeciality(Integer id) {
        return specialityRepository.findById(id)
                .orElseThrow(() -> new SpecialityNotFoundException("Speciality with Id " + id + " not found"));
    }

    public SpecialityDoctorMap mapDoctorToSpeciality(Integer specialityId, Integer doctorId) {

        // validate both exist
        specialityRepository.findById(specialityId)
                .orElseThrow(() -> new SpecialityNotFoundException("Speciality with Id " + specialityId + " not found"));

        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with Id " + doctorId + " not found"));

        SpecialityDoctorMapId id = new SpecialityDoctorMapId(specialityId, doctorId);
        if (mapRepository.existsById(id)) {
            // already mapped -> just return existing mapping object (simple behavior)
            SpecialityDoctorMap existing = new SpecialityDoctorMap();
            existing.setSpecialityId(specialityId);
            existing.setDoctorId(doctorId);
            return existing;
        }

        SpecialityDoctorMap map = new SpecialityDoctorMap();
        map.setSpecialityId(specialityId);
        map.setDoctorId(doctorId);

        return mapRepository.save(map);
    }

    public void removeDoctorFromSpeciality(Integer specialityId, Integer doctorId) {
        SpecialityDoctorMapId id = new SpecialityDoctorMapId(specialityId, doctorId);
        mapRepository.deleteById(id);
    }

    public List<Doctor> getDoctorsBySpeciality(Integer specialityId) {

        // ensure speciality exists
        specialityRepository.findById(specialityId)
                .orElseThrow(() -> new SpecialityNotFoundException("Speciality with Id " + specialityId + " not found"));

        List<SpecialityDoctorMap> mappings = mapRepository.findBySpecialityId(specialityId);
        List<Integer> doctorIds = new ArrayList<>();

        for (SpecialityDoctorMap m : mappings) {
            doctorIds.add(m.getDoctorId());
        }

        return doctorRepository.findAllById(doctorIds);
    }
}
