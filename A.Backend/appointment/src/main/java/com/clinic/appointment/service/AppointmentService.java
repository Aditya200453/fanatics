package com.clinic.appointment.service;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // Patient books -> PENDING
    Appointment bookForLoggedInPatient(String patientEmail, BookAppointmentRequest req);

    // Patient sees own appointments (PENDING + BOOKED)
    List<Appointment> myAppointments(String patientEmail);

    // Doctor sees only BOOKED appointments
    List<Appointment> doctorAppointments(String doctorEmail);

    // Staff/Admin reports
    List<Appointment> appointmentsByPatientId(Integer patientId);

    List<Appointment> appointmentsByDoctorIdAndDate(Integer doctorId, LocalDate date);

    // Staff queue
    List<Appointment> pendingAppointments();

    // Staff approves PENDING -> BOOKED
    Appointment approveAppointment(Integer appointmentId);
}