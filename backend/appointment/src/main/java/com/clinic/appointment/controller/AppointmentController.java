package com.clinic.appointment.controller;

import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.service.AppointmentService;
import com.clinic.appointment.util.ResponseMessage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    private AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // CRUD + listing

    @GetMapping("/")
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointment(id));
    }

    // Add a visit / book appointment (Requirement) 【1-87918d】
    @PostMapping("/")
    public ResponseEntity<Appointment> book(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    @PutMapping("/")
    public ResponseEntity<Appointment> update(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(appointment));
    }

    @DeleteMapping("/")
    public ResponseEntity<ResponseMessage> delete(@RequestParam Integer id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new ResponseMessage("Appointment deleted"));
    }

    // List all appointments of a patient (Requirement) 【1-87918d】
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getByPatient(@PathVariable Integer patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    // List appointments for a doctor on a particular date (Requirement) 【1-87918d】
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getByDoctorAndDate(
            @PathVariable Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorAndDate(doctorId, date));
    }
}
