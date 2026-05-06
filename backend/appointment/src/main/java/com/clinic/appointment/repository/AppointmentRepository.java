package com.clinic.appointment.repository;

import com.clinic.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
            Integer doctorId, LocalDate date, LocalTime time, List<String> statuses);

    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Integer patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(Integer doctorId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDesc(Integer patientId);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Integer patientId);


}