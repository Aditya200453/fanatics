package com.clinic.appointment.service;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;

import java.util.List;

public interface AppointmentService {

    // ✅ Book appointment (no slots)
    Appointment bookForLoggedInPatient(String patientEmail,
                                       BookAppointmentRequest req);

    // ✅ Patient → view own appointments
    List<Appointment> myAppointments(String patientEmail);

    // ✅ Doctor → view appointments
    List<Appointment> doctorAppointments(String doctorEmail);
}
