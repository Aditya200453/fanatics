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

    private final AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment getAppointment(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment with Id " + id + " not found"));
    }

    // ✅ CREATE (BOOK)
    @Override
    public Appointment bookAppointment(Appointment appointment) {
        validateMandatoryFields(appointment);

        // Align with DB default
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus("BOOKED");
        }

        Integer patientId = appointment.getPatientId();
        Integer doctorId = appointment.getDoctorId();
        LocalDate date = appointment.getAppointmentDate();
        LocalTime time = appointment.getAppointmentTime();

        // Doctor slot conflict
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(doctorId, date, time)) {
            throw new AppointmentConflictException("Doctor already has an appointment at " + date + " " + time);
        }

        // Patient slot conflict
        if (appointmentRepository.existsByPatientIdAndAppointmentDateAndAppointmentTime(patientId, date, time)) {
            throw new AppointmentConflictException("Patient already has an appointment at " + date + " " + time);
        }

        return appointmentRepository.save(appointment);
    }

    // ✅ UPDATE (FULL)
    @Override
    public Appointment updateAppointment(Appointment appointment) {
        validateMandatoryFields(appointment);

        Integer apptId = appointment.getAppointmentId();

        // ✅ FIXED BUG: proper condition
        if (apptId == null || !appointmentRepository.existsById(apptId)) {
            throw new AppointmentNotFoundException("Appointment not found for update");
        }

        // Preserve createdAt (audit safety)
        Appointment existing = getAppointment(apptId);
        appointment.setCreatedAt(existing.getCreatedAt());

        // Align with DB default
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            appointment.setStatus(existing.getStatus() != null ? existing.getStatus() : "BOOKED");
        }

        Integer patientId = appointment.getPatientId();
        Integer doctorId = appointment.getDoctorId();
        LocalDate date = appointment.getAppointmentDate();
        LocalTime time = appointment.getAppointmentTime();

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(doctorId, date, time, apptId)) {
            throw new AppointmentConflictException("Doctor already has an appointment at " + date + " " + time);
        }

        if (appointmentRepository.existsByPatientIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(patientId, date, time, apptId)) {
            throw new AppointmentConflictException("Patient already has an appointment at " + date + " " + time);
        }

        return appointmentRepository.save(appointment);
    }

    // ✅ PATCH (PARTIAL)
    @Override
    public Appointment patchAppointment(Integer id, Appointment patch) {
        Appointment existing = getAppointment(id);

        // Only apply non-null fields
        if (patch.getPatientId() != null) existing.setPatientId(patch.getPatientId());
        if (patch.getDoctorId() != null) existing.setDoctorId(patch.getDoctorId());
        if (patch.getAppointmentDate() != null) existing.setAppointmentDate(patch.getAppointmentDate());
        if (patch.getAppointmentTime() != null) existing.setAppointmentTime(patch.getAppointmentTime());
        if (patch.getStatus() != null && !patch.getStatus().isBlank()) existing.setStatus(patch.getStatus());
        if (patch.getSymptoms() != null) existing.setSymptoms(patch.getSymptoms());
        if (patch.getRemarks() != null) existing.setRemarks(patch.getRemarks());

        // Re-check conflicts only if slot-related fields are present or changed
        validateMandatoryFields(existing);

        Integer patientId = existing.getPatientId();
        Integer doctorId = existing.getDoctorId();
        LocalDate date = existing.getAppointmentDate();
        LocalTime time = existing.getAppointmentTime();

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(doctorId, date, time, id)) {
            throw new AppointmentConflictException("Doctor already has an appointment at " + date + " " + time);
        }
        if (appointmentRepository.existsByPatientIdAndAppointmentDateAndAppointmentTimeAndAppointmentIdNot(patientId, date, time, id)) {
            throw new AppointmentConflictException("Patient already has an appointment at " + date + " " + time);
        }

        return appointmentRepository.save(existing);
    }

    @Override
    public void deleteAppointment(Integer id) {
        if (!appointmentRepository.existsById(id)) {
            throw new AppointmentNotFoundException("Appointment with Id " + id + " not found");
        }
        appointmentRepository.deleteById(id);
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(Integer patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    @Override
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
