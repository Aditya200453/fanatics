package com.clinic.appointment.service;

import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.exception.AppointmentConflictException;
import com.clinic.appointment.exception.AppointmentNotFoundException;
import com.clinic.appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Appointment getAppointment(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment with Id " + id + " not found"));
    }

    // ✅ CREATE (BOOK)
    public Appointment bookAppointment(Appointment appointment) {

        validateMandatoryFields(appointment);

        Integer patientId = appointment.getPatientId();
        Integer doctorId = appointment.getDoctorId();
        LocalDate date = appointment.getAppointmentDate();
        LocalTime time = appointment.getAppointmentTime();

        // Constraint 1: Doctor slot must be free (no two patients same doctor same date+time)
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(doctorId, date, time)) {
            throw new AppointmentConflictException("Doctor already has an appointment at " + date + " " + time);
        }

        // Constraint 3: Patient cannot have another appointment at same date+time (even different doctor)
        if (appointmentRepository.existsByPatientIdAndAppointmentDateAndAppointmentTime(patientId, date, time)) {
            throw new AppointmentConflictException("Patient already has an appointment at " + date + " " + time);
        }

        // (Constraint 2 is satisfied by allowing same patient multiple appointments on same day at different times)

        return appointmentRepository.save(appointment);
    }

    // ✅ UPDATE
    public Appointment updateAppointment(Appointment appointment) {

        validateMandatoryFields(appointment);

        Integer apptId = appointment.getAppointmentId();
        if (apptId == null || !appointmentRepository.existsById(apptId)) {
            throw new AppointmentNotFoundException("Appointment not found for update");
        }

        Integer patientId = appointment.getPatientId();
        Integer doctorId = appointment.getDoctorId();
        LocalDate date = appointment.getAppointmentDate();
        LocalTime time = appointment.getAppointmentTime();

        // Constraint 1 (Update): exclude same record
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(doctorId, date, time, apptId)) {
            throw new AppointmentConflictException("Doctor already has an appointment at " + date + " " + time);
        }

        // Constraint 3 (Update): exclude same record
        if (appointmentRepository.existsByPatientIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(patientId, date, time, apptId)) {
            throw new AppointmentConflictException("Patient already has an appointment at " + date + " " + time);
        }

        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(Integer id) {
        if (!appointmentRepository.existsById(id)) {
            throw new AppointmentNotFoundException("Appointment with Id " + id + " not found");
        }
        appointmentRepository.deleteById(id);
    }

    public List<Appointment> getAppointmentsByPatient(Integer patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getAppointmentsByDoctorAndDate(Integer doctorId, LocalDate date) {
        return appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
    }

    private void validateMandatoryFields(Appointment appointment) {
        if (appointment.getPatientId() == null) {
            throw new AppointmentConflictException("patientId is required");
        }
        if (appointment.getDoctorId() == null) {
            throw new AppointmentConflictException("doctorId is required");
        }
        if (appointment.getAppointmentDate() == null) {
            throw new AppointmentConflictException("appointmentDate is required");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new AppointmentConflictException("appointmentTime is required");
        }
    }
}