package com.clinic.appointment.service;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository repo;
    private final RestTemplate restTemplate;

    @Value("${app.internal-key}")
    private String internalKey;

    public AppointmentServiceImpl(AppointmentRepository repo,
                                  RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    // ✅ ✅ BOOK APPOINTMENT
    @Override
    public Appointment bookForLoggedInPatient(String patientEmail,
                                              BookAppointmentRequest req) {

        // ✅ Get patientId (internal API)
        Integer patientId = getPatientId(patientEmail);

        // ✅ Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(req.getDoctorId());
        appointment.setAppointmentDate(req.getAppointmentDate());
        appointment.setAppointmentTime(req.getAppointmentTime());
        appointment.setStatus("BOOKED");
        appointment.setSymptoms(req.getSymptoms());
        appointment.setRemarks(null);

        // ✅ Save in DB
        return repo.save(appointment);
    }

    // ✅ ✅ PATIENT APPOINTMENTS
    @Override
    public List<Appointment> myAppointments(String patientEmail) {

        Integer patientId = getPatientId(patientEmail);

        return repo.findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    // ✅ ✅ DOCTOR APPOINTMENTS
    @Override
    public List<Appointment> doctorAppointments(String doctorEmail) {

        Integer doctorId = getDoctorId(doctorEmail);

        return repo.findByDoctorIdOrderByAppointmentDateDesc(doctorId);
    }

    // ✅ ✅ INTERNAL CALL → PATIENT ID
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

    // ✅ ✅ INTERNAL CALL → DOCTOR ID
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