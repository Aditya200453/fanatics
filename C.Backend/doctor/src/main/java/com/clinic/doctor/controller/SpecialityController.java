package com.clinic.doctor.controller;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.service.SpecialityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/speciality")
public class SpecialityController {

    private final SpecialityService specialityService;

    public SpecialityController(SpecialityService specialityService) {
        this.specialityService = specialityService;
    }

    // =====================================================
    // ✅ ADMIN ONLY – CREATE SPECIALITY (MASTER DATA)
    // =====================================================
    @PostMapping("/")
    public ResponseEntity<Speciality> addSpeciality(
            @RequestHeader("X-User-Role") String role,
            @RequestBody Speciality speciality) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(specialityService.addSpeciality(speciality));
    }

    // =====================================================
    // ✅ READ‑ONLY – ADMIN + STAFF + PATIENT
    // Used by: patient booking, staff/admin dashboards
    // =====================================================
    @GetMapping("/")
    public ResponseEntity<List<Speciality>> getAllSpecialities(
            @RequestHeader("X-User-Role") String role) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN")
                || role.equalsIgnoreCase("STAFF")
                || role.equalsIgnoreCase("PATIENT"))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(specialityService.getAllSpecialities());
    }

    // =====================================================
    // ✅ READ‑ONLY – ADMIN + STAFF + PATIENT
    // Used by: appointment booking doctor selection
    // =====================================================
    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Doctor>> getDoctorsBySpeciality(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String role) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN")
                || role.equalsIgnoreCase("STAFF")
                || role.equalsIgnoreCase("PATIENT"))) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(specialityService.getDoctorsBySpeciality(id));
    }

    // =====================================================
    // ✅ ADMIN + STAFF – MAP DOCTOR → SPECIALITY
    // =====================================================
    @PostMapping("/map")
    public ResponseEntity<SpecialityDoctorMap> mapDoctor(
            @RequestHeader("X-User-Role") String role,
            @RequestBody SpecialityDoctorMap map) {

        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(
                specialityService.mapDoctorToSpeciality(
                        map.getSpecialityId(),
                        map.getDoctorId()
                )
        );
    }

    // =====================================================
    // ✅ ADMIN + STAFF – REMOVE DOCTOR FROM SPECIALITY
    // =====================================================
    @DeleteMapping("/map")
    public ResponseEntity<Void> unmapDoctor(
            @RequestHeader("X-User-Role") String role,
            @RequestParam Integer specialityId,
            @RequestParam Integer doctorId) {

        if (!(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        specialityService.removeDoctorFromSpeciality(specialityId, doctorId);
        return ResponseEntity.ok().build();
    }
    // ✅ DELETE SPECIALITY (ADMIN ONLY)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpeciality(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Integer id) {

        if (!"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        specialityService.deleteSpeciality(id);
        return ResponseEntity.ok().build();
    }
}