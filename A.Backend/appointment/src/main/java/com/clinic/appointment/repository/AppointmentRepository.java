package com.clinic.appointment.repository;

import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Patient's full history (includes PENDING + BOOKED)
    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Integer patientId);

    // Doctor schedule (BOOKED only)
    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateDesc(Integer doctorId, AppointmentStatus status);

    // Staff pending queue
    List<Appointment> findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus status);

    // Staff: doctor schedule for a date (BOOKED only)
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusOrderByAppointmentTimeAsc(
            Integer doctorId, LocalDate appointmentDate, AppointmentStatus status
    );
}