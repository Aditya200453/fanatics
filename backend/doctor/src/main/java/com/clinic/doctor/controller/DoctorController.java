package com.clinic.doctor.controller;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Doctor>> getAll() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(doctorService.getDoctor(id));
    }

    @PostMapping("/")
    public ResponseEntity<Doctor> add(@RequestBody Doctor doctor) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorService.saveDoctor(doctor));
    }

    @PutMapping("/")
    public ResponseEntity<Doctor> update(@RequestBody Doctor doctor) {
        return ResponseEntity.ok(doctorService.updateDoctor(doctor));
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> delete(@RequestParam Integer id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok().build();
    }
}