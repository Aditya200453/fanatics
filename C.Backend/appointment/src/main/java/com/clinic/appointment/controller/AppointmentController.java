package com.clinic.appointment.controller;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    //  PATIENT: Book -> PENDING
    @PostMapping("/book")
    public ResponseEntity<Appointment> book(
            @RequestHeader("X-User-Email") String email,
            @RequestBody BookAppointmentRequest req) {
        return ResponseEntity.ok(service.bookForLoggedInPatient(email, req));
    }

    //  PATIENT: My appointments (all statuses)
    @GetMapping("/my")
    public ResponseEntity<List<Appointment>> my(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(service.myAppointments(email));
    }

    //  DOCTOR: My appointments (BOOKED only)
    @GetMapping("/doctor/my")
    public ResponseEntity<List<Appointment>> doctorMy(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(service.doctorAppointments(email));
    }

    // DOCTOR: add checkup notes/discussion (remarks)
    @PutMapping("/doctor/remarks/{appointmentId}")
    public ResponseEntity<Appointment> updateRemarks(
            @RequestHeader("X-User-Email") String doctorEmail,
            @PathVariable Integer appointmentId,
            @RequestBody String remarks) {

        return ResponseEntity.ok(service.updateRemarksByDoctor(appointmentId, remarks, doctorEmail));
    }

    //  STAFF/ADMIN: Pending appointments queue
    @GetMapping("/staff/pending")
    public ResponseEntity<List<Appointment>> pending(
            @RequestHeader("X-User-Role") String role) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.pendingAppointments());
    }

    //  STAFF/ADMIN: Approve appointment (PENDING -> BOOKED)
    @PutMapping("/staff/{appointmentId}/approve")
    public ResponseEntity<Appointment> approve(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Integer appointmentId) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.approveAppointment(appointmentId));
    }

    //  STAFF/ADMIN: appointments by patientId
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> appointmentsByPatientId(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Integer patientId) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(service.appointmentsByPatientId(patientId));
    }

    //  STAFF/ADMIN: doctor schedule by date
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> appointmentsByDoctorAndDate(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Integer doctorId,
            @RequestParam String date) {

        if (role == null || !(role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("STAFF"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(service.appointmentsByDoctorIdAndDate(doctorId, parsedDate));
    }
}
