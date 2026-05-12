package com.clinic.appointment.service;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.entity.AppointmentStatus;
import com.clinic.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repo;
    private final RestTemplate restTemplate;

    @Value("${app.internal-key}")
    private String internalKey;

    public AppointmentServiceImpl(AppointmentRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    // ✅ Patient books -> ALWAYS PENDING
    @Override
    public Appointment bookForLoggedInPatient(String patientEmail, BookAppointmentRequest req) {

        Integer patientId = getPatientId(patientEmail);

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(req.getDoctorId());
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setAppointmentTime(req.getAppointmentTime());

        // ✅ NEW FLOW: PENDING until staff approves
        appointment.setStatus(AppointmentStatus.PENDING);

        appointment.setSymptoms(req.getSymptoms());
        appointment.setRemarks(null);

        return repo.save(appointment);
    }

    // ✅ Patient sees PENDING + BOOKED
    @Override
    public List<Appointment> myAppointments(String patientEmail) {
        Integer patientId = getPatientId(patientEmail);
        return repo.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    // ✅ Doctor sees ONLY BOOKED
    @Override
    public List<Appointment> doctorAppointments(String doctorEmail) {
        Integer doctorId = getDoctorId(doctorEmail);
        return repo.findByDoctorIdAndStatusOrderByAppointmentDateDesc(doctorId, AppointmentStatus.BOOKED);
    }

    // ✅ Staff/Admin: all appointments for a patient (both statuses)
    @Override
    public List<Appointment> appointmentsByPatientId(Integer patientId) {
        return repo.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    // ✅ Staff/Admin: doctor schedule for a date (BOOKED only)
    @Override
    public List<Appointment> appointmentsByDoctorIdAndDate(Integer doctorId, LocalDate date) {
        return repo.findByDoctorIdAndAppointmentDateAndStatusOrderByAppointmentTimeAsc(
                doctorId, date, AppointmentStatus.BOOKED
        );
    }

    // ✅ Staff: pending queue
    @Override
    public List<Appointment> pendingAppointments() {
        return repo.findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus.PENDING);
    }

    // ✅ Staff approves: PENDING -> BOOKED
    @Override
    public Appointment approveAppointment(Integer appointmentId) {
        Appointment appt = repo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

        if (appt.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only PENDING appointments can be approved");
        }

        appt.setStatus(AppointmentStatus.BOOKED);
        return repo.save(appt);
    }

    // ---------------- INTERNAL CALLS ----------------

    private Integer getPatientId(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INTERNAL-KEY", internalKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://patient/patient/internal/id?email=" + email,
                HttpMethod.GET,
                entity,
                Integer.class
        ).getBody();
    }

    private Integer getDoctorId(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-INTERNAL-KEY", internalKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                "http://doctor/doctor/internal/id?email=" + email,
                HttpMethod.GET,
                entity,
                Integer.class
        ).getBody();
    }
}
