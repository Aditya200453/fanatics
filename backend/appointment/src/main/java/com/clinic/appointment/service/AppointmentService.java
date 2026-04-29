package com.clinic.appointment.service;

import com.clinic.appointment.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    List<Appointment> getAllAppointments();
    Appointment getAppointment(Integer id);

    Appointment bookAppointment(Appointment appointment); // Create
    Appointment updateAppointment(Appointment appointment); // Update (full)

    Appointment patchAppointment(Integer id, Appointment patch); // Partial update (PATCH)

    void deleteAppointment(Integer id);

    List<Appointment> getAppointmentsByPatient(Integer patientId);
    List<Appointment> getAppointmentsByDoctorAndDate(Integer doctorId, LocalDate date);
}
