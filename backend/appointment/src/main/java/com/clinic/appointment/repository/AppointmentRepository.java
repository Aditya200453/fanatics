package com.clinic.appointment.repository;

import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Patient
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Integer patientId);

    // Doctor: only BOOKED (or a specific status)
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateDesc(
            Integer doctorId,
            AppointmentStatus status
    );

    // Staff/Admin: pending queue
    List<Appointment> findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus status);

    // Staff/Admin: doctor schedule on a date (BOOKED only)
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusOrderByAppointmentTimeAsc(
            Integer doctorId,
            LocalDate appointmentDate,
            AppointmentStatus status
    );

    // Optional: overlap check (if you want to block duplicates)
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
            Integer doctorId,
            LocalDate date,
            LocalTime time,
            List<AppointmentStatus> statuses
    );
}
