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

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/")
    public ResponseEntity<List<Appointment>> getAll() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(appointmentService.getAppointment(id));
    }

    @PostMapping("/")
    public ResponseEntity<Appointment> book(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
    }

    @PutMapping("/")
    public ResponseEntity<Appointment> update(@RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.updateAppointment(appointment));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Appointment> patch(@PathVariable Integer id, @RequestBody Appointment patch) {
        return ResponseEntity.ok(appointmentService.patchAppointment(id, patch));
    }

    @DeleteMapping("/")
    public ResponseEntity<ResponseMessage> delete(@RequestParam Integer id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(new ResponseMessage("Appointment deleted"));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getByPatient(@PathVariable Integer patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getByDoctorAndDate(
            @PathVariable Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorAndDate(doctorId, date));
    }
}
