package com.clinic.appointment;

import com.clinic.appointment.controller.AppointmentController;
import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerUnitTest {

    @Mock
    private AppointmentService service;

    @InjectMocks
    private AppointmentController controller;

    // ---------------- PATIENT ----------------

    @Test
    void book_shouldReturn200_andCallService() {
        String email = "patient@mail.com";
        BookAppointmentRequest req = mock(BookAppointmentRequest.class);

        Appointment saved = new Appointment();
        when(service.bookForLoggedInPatient(eq(email), any(BookAppointmentRequest.class))).thenReturn(saved);

        ResponseEntity<Appointment> resp = controller.book(email, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(saved, resp.getBody());
        verify(service).bookForLoggedInPatient(eq(email), same(req));
    }

    @Test
    void my_shouldReturn200_andList() {
        String email = "patient@mail.com";
        List<Appointment> list = List.of(new Appointment(), new Appointment());
        when(service.myAppointments(email)).thenReturn(list);

        ResponseEntity<List<Appointment>> resp = controller.my(email);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(list, resp.getBody());
        verify(service).myAppointments(email);
    }

    // ---------------- DOCTOR ----------------

    @Test
    void doctorMy_shouldReturn200_andList() {
        String email = "doctor@mail.com";
        List<Appointment> list = List.of(new Appointment());
        when(service.doctorAppointments(email)).thenReturn(list);

        ResponseEntity<List<Appointment>> resp = controller.doctorMy(email);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(list, resp.getBody());
        verify(service).doctorAppointments(email);
    }

    @Test
    void updateRemarks_shouldReturn200_andCallService() {
        String doctorEmail = "doctor@mail.com";
        Integer appointmentId = 10;

        Appointment updated = new Appointment();
        when(service.updateRemarksByDoctor(eq(appointmentId), eq("Notes"), eq(doctorEmail))).thenReturn(updated);

        ResponseEntity<Appointment> resp =
                controller.updateRemarks(doctorEmail, appointmentId, "Notes");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(updated, resp.getBody());
        verify(service).updateRemarksByDoctor(eq(appointmentId), eq("Notes"), eq(doctorEmail));
    }

    // ---------------- STAFF/ADMIN ----------------

    @Test
    void pending_admin_shouldReturn200() {
        List<Appointment> list = List.of(new Appointment());
        when(service.pendingAppointments()).thenReturn(list);

        ResponseEntity<List<Appointment>> resp = controller.pending("ADMIN");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(list, resp.getBody());
        verify(service).pendingAppointments();
    }

    @Test
    void pending_staff_shouldReturn200() {
        when(service.pendingAppointments()).thenReturn(List.of());

        ResponseEntity<List<Appointment>> resp = controller.pending("STAFF");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(service).pendingAppointments();
    }

    @Test
    void pending_wrongRole_shouldReturn403_andNotCallService() {
        ResponseEntity<List<Appointment>> resp = controller.pending("PATIENT");

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(service, never()).pendingAppointments();
    }

    @Test
    void pending_nullRole_shouldReturn403_andNotCallService() {
        ResponseEntity<List<Appointment>> resp = controller.pending(null);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(service, never()).pendingAppointments();
    }

    @Test
    void approve_admin_shouldReturn200() {
        Appointment appt = new Appointment();
        when(service.approveAppointment(44)).thenReturn(appt);

        ResponseEntity<Appointment> resp = controller.approve("ADMIN", 44);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(appt, resp.getBody());
        verify(service).approveAppointment(44);
    }

    @Test
    void approve_wrongRole_shouldReturn403_andNotCallService() {
        ResponseEntity<Appointment> resp = controller.approve("DOCTOR", 44);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(service, never()).approveAppointment(anyInt());
    }

    @Test
    void appointmentsByPatientId_staff_shouldReturn200() {
        when(service.appointmentsByPatientId(101)).thenReturn(List.of());

        ResponseEntity<List<Appointment>> resp = controller.appointmentsByPatientId("STAFF", 101);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(service).appointmentsByPatientId(101);
    }

    @Test
    void appointmentsByDoctorAndDate_validDate_admin_shouldReturn200() {
        when(service.appointmentsByDoctorIdAndDate(eq(77), eq(LocalDate.parse("2026-05-15"))))
                .thenReturn(List.of());

        ResponseEntity<List<Appointment>> resp =
                controller.appointmentsByDoctorAndDate("ADMIN", 77, "2026-05-15");

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        verify(service).appointmentsByDoctorIdAndDate(eq(77), eq(LocalDate.parse("2026-05-15")));
    }

    @Test
    void appointmentsByDoctorAndDate_invalidDate_shouldReturn400_andNotCallService() {
        ResponseEntity<List<Appointment>> resp =
                controller.appointmentsByDoctorAndDate("ADMIN", 77, "15-05-2026");

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(service, never()).appointmentsByDoctorIdAndDate(anyInt(), ArgumentMatchers.any(LocalDate.class));
    }

    @Test
    void appointmentsByDoctorAndDate_wrongRole_shouldReturn403_andNotCallService() {
        ResponseEntity<List<Appointment>> resp =
                controller.appointmentsByDoctorAndDate("PATIENT", 77, "2026-05-15");

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertNull(resp.getBody());
        verify(service, never()).appointmentsByDoctorIdAndDate(anyInt(), any(LocalDate.class));
    }
}