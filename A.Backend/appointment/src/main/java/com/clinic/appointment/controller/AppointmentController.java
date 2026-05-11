package com.clinic.appointment.controller;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    // ✅ Book appointment
    @PostMapping("/book")
    public ResponseEntity<Appointment> book(
            @RequestHeader("X-User-Email") String email,
            @RequestBody BookAppointmentRequest req) {

        return ResponseEntity.ok(service.bookForLoggedInPatient(email, req));
    }

    // ✅ Patient appointments
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> my(
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(service.myAppointments(email));
    }

    // ✅ Doctor appointments (optional)
    @GetMapping("/doctor/my")
    public ResponseEntity<List<Appointment>> doctorMy(
            @RequestHeader("X-User-Email") String email) {

        return ResponseEntity.ok(service.doctorAppointments(email));
    }

}
