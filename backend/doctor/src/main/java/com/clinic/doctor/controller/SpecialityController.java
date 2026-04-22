package com.clinic.doctor.controller;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.service.SpecialityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/speciality")
public class SpecialityController {

    private SpecialityService specialityService;

    public SpecialityController(SpecialityService specialityService) {
        this.specialityService = specialityService;
    }

    // Add Speciality -> POST http://localhost:8072/speciality/
    @PostMapping("/")
    public ResponseEntity<Speciality> addSpeciality(@RequestBody Speciality speciality) {
        return ResponseEntity.ok(specialityService.addSpeciality(speciality));
    }

    // Get ALL -> GET http://localhost:8072/speciality/
    @GetMapping("/")
    public ResponseEntity<List<Speciality>> getAllSpecialities() {
        return ResponseEntity.ok(specialityService.getAllSpecialities());
    }

    // Get by ID -> GET http://localhost:8072/speciality/1
    @GetMapping("/{id}")
    public ResponseEntity<Speciality> getSpeciality(@PathVariable Integer id) {
        return ResponseEntity.ok(specialityService.getSpeciality(id));
    }

    // Map Doctor -> POST http://localhost:8072/speciality/map
    // Body: { "specialityId":1, "doctorId":6 }
    @PostMapping("/map")
    public ResponseEntity<SpecialityDoctorMap> mapDoctor(@RequestBody SpecialityDoctorMap map) {
        return ResponseEntity.ok(
                specialityService.mapDoctorToSpeciality(map.getSpecialityId(), map.getDoctorId())
        );
    }

    // Remove mapping -> DELETE http://localhost:8072/speciality/map?specialityId=1&doctorId=6
    @DeleteMapping("/map")
    public ResponseEntity<Void> unmapDoctor(@RequestParam Integer specialityId,
                                            @RequestParam Integer doctorId) {
        specialityService.removeDoctorFromSpeciality(specialityId, doctorId);
        return ResponseEntity.ok().build();
    }

    // Get Doctors by Speciality -> GET http://localhost:8072/speciality/1/doctors
    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Doctor>> getDoctorsBySpeciality(@PathVariable Integer id) {
        return ResponseEntity.ok(specialityService.getDoctorsBySpeciality(id));
    }
}