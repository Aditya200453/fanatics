package com.clinic.appointment.repository;

import com.clinic.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // For required listing features
    List<Appointment> findByPatientId(Integer patientId);
    List<Appointment> findByDoctorIdAndAppointmentDate(Integer doctorId, LocalDate appointmentDate);

    // Constraint checks (Create)
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(Integer doctorId, LocalDate appointmentDate, LocalTime appointmentTime);
    boolean existsByPatientIdAndAppointmentDateAndAppointmentTime(Integer patientId, LocalDate appointmentDate, LocalTime appointmentTime);

    // Constraint checks (Update) - exclude current appointmentId
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(Integer doctorId, LocalDate appointmentDate, LocalTime appointmentTime, Integer appointmentId);
    boolean existsByPatientIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(Integer patientId, LocalDate appointmentDate, LocalTime appointmentTime, Integer appointmentId);
}
