package com.clinic.appointment.service;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // Patient books -> PENDING
    Appointment bookForLoggedInPatient(String patientEmail, BookAppointmentRequest req);

    // Patient views appointments (PENDING + BOOKED + etc.)
    List<Appointment> myAppointments(String patientEmail);

    // Doctor views only BOOKED appointments
    List<Appointment> doctorAppointments(String doctorEmail);

    // Staff/Admin: pending queue
    List<Appointment> pendingAppointments();

    // Staff/Admin: approve PENDING -> BOOKED
    Appointment approveAppointment(Integer appointmentId);

    // Staff/Admin reports
    List<Appointment> appointmentsByPatientId(Integer patientId);
    List<Appointment> appointmentsByDoctorIdAndDate(Integer doctorId, LocalDate date);

    // Doctor adds checkup notes / discussion
    Appointment updateRemarksByDoctor(Integer appointmentId, String remarks, String doctorEmail);
}