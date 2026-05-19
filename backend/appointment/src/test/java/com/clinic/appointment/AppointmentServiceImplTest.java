package com.clinic.appointment;

import com.clinic.appointment.dto.BookAppointmentRequest;
import com.clinic.appointment.entity.Appointment;
import com.clinic.appointment.entity.AppointmentStatus;
import com.clinic.appointment.repository.AppointmentRepository;
import com.clinic.appointment.service.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository repo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AppointmentServiceImpl service;

    @BeforeEach
    void setUp() {
        // ✅ set @Value field (internal key) for unit tests
        ReflectionTestUtils.setField(service, "internalKey", "test-internal-key");
    }

    // -------------------------
    // Helper methods
    // -------------------------

    private void mockPatientIdLookup(String email, Integer patientId) {
        String url = "http://patient/patient/internal/id?email=" + email;

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Integer.class)
        )).thenReturn(ResponseEntity.ok(patientId));
    }

    private void mockDoctorIdLookup(String email, Integer doctorId) {
        String url = "http://doctor/doctor/internal/id?email=" + email;

        when(restTemplate.exchange(
                eq(url),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Integer.class)
        )).thenReturn(ResponseEntity.ok(doctorId));
    }

    private static Appointment appt(Integer patientId, Integer doctorId, AppointmentStatus status) {
        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setDoctorId(doctorId);
        a.setStatus(status);
        return a;
    }

    // -------------------------
    // Tests
    // -------------------------

    @Test
    void bookForLoggedInPatient_shouldSavePendingAppointment() {
        // Arrange
        String patientEmail = "p1@mail.com";
        Integer patientId = 101;
        mockPatientIdLookup(patientEmail, patientId);

        BookAppointmentRequest req = mock(BookAppointmentRequest.class);
        when(req.getDoctorId()).thenReturn(501);
        when(req.getSymptoms()).thenReturn("Fever");

        // Date/time types might vary in your DTO/entity; leaving them as null is still valid for unit test
        when(req.getAppointmentDate()).thenReturn(null);
        when(req.getAppointmentTime()).thenReturn(null);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);

        when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Appointment saved = service.bookForLoggedInPatient(patientEmail, req);

        // Assert
        verify(repo).save(captor.capture());
        Appointment toSave = captor.getValue();

        assertEquals(patientId, toSave.getPatientId());
        assertEquals(501, toSave.getDoctorId());
        assertEquals(AppointmentStatus.PENDING, toSave.getStatus());
        assertEquals("Fever", toSave.getSymptoms());
        assertNull(toSave.getRemarks());

        assertEquals(AppointmentStatus.PENDING, saved.getStatus());

        // ✅ Verify internal header was sent (X-INTERNAL-KEY)
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq("http://patient/patient/internal/id?email=" + patientEmail),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(Integer.class)
        );

        HttpEntity capturedEntity = entityCaptor.getValue();
        assertNotNull(capturedEntity);
        assertTrue(capturedEntity.getHeaders().containsHeader("X-INTERNAL-KEY"));
        assertEquals("test-internal-key", capturedEntity.getHeaders().getFirst("X-INTERNAL-KEY"));
    }

    @Test
    void myAppointments_shouldReturnAppointmentsForLoggedInPatient() {
        // Arrange
        String patientEmail = "p2@mail.com";
        Integer patientId = 202;
        mockPatientIdLookup(patientEmail, patientId);

        List<Appointment> expected = List.of(
                appt(patientId, 11, AppointmentStatus.PENDING),
                appt(patientId, 12, AppointmentStatus.BOOKED)
        );

        when(repo.findByPatientIdOrderByAppointmentDateDesc(patientId)).thenReturn(expected);

        // Act
        List<Appointment> result = service.myAppointments(patientEmail);

        // Assert
        assertEquals(2, result.size());
        assertSame(expected, result);
        verify(repo).findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Test
    void doctorAppointments_shouldReturnOnlyBookedAppointments() {
        // Arrange
        String doctorEmail = "d1@mail.com";
        Integer doctorId = 303;
        mockDoctorIdLookup(doctorEmail, doctorId);

        List<Appointment> expected = List.of(
                appt(1, doctorId, AppointmentStatus.BOOKED),
                appt(2, doctorId, AppointmentStatus.BOOKED)
        );

        when(repo.findByDoctorIdAndStatusOrderByAppointmentDateDesc(doctorId, AppointmentStatus.BOOKED))
                .thenReturn(expected);

        // Act
        List<Appointment> result = service.doctorAppointments(doctorEmail);

        // Assert
        assertSame(expected, result);
        verify(repo).findByDoctorIdAndStatusOrderByAppointmentDateDesc(doctorId, AppointmentStatus.BOOKED);
    }

    @Test
    void pendingAppointments_shouldReturnPendingQueue() {
        // Arrange
        List<Appointment> expected = List.of(
                appt(1, 1, AppointmentStatus.PENDING),
                appt(2, 2, AppointmentStatus.PENDING)
        );

        when(repo.findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus.PENDING))
                .thenReturn(expected);

        // Act
        List<Appointment> result = service.pendingAppointments();

        // Assert
        assertSame(expected, result);
        verify(repo).findByStatusOrderByAppointmentDateAscAppointmentTimeAsc(AppointmentStatus.PENDING);
    }

    @Test
    void approveAppointment_shouldMovePendingToBooked() {
        // Arrange
        Integer apptId = 10;
        Appointment pending = appt(1, 2, AppointmentStatus.PENDING);

        when(repo.findById(apptId)).thenReturn(Optional.of(pending));
        when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Appointment result = service.approveAppointment(apptId);

        // Assert
        assertEquals(AppointmentStatus.BOOKED, result.getStatus());
        verify(repo).save(pending);
    }

    @Test
    void approveAppointment_shouldThrowIfNotPending() {
        // Arrange
        Integer apptId = 11;
        Appointment booked = appt(1, 2, AppointmentStatus.BOOKED);

        when(repo.findById(apptId)).thenReturn(Optional.of(booked));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.approveAppointment(apptId));
        assertTrue(ex.getMessage().contains("Only PENDING appointments can be approved"));

        verify(repo, never()).save(any());
    }

    @Test
    void approveAppointment_shouldThrowIfNotFound() {
        // Arrange
        Integer apptId = 12;
        when(repo.findById(apptId)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.approveAppointment(apptId));
        assertTrue(ex.getMessage().contains("Appointment not found"));

        verify(repo, never()).save(any());
    }

    @Test
    void appointmentsByPatientId_shouldReturnList() {
        // Arrange
        Integer patientId = 999;
        List<Appointment> expected = List.of(appt(patientId, 1, AppointmentStatus.PENDING));
        when(repo.findByPatientIdOrderByAppointmentDateDesc(patientId)).thenReturn(expected);

        // Act
        List<Appointment> result = service.appointmentsByPatientId(patientId);

        // Assert
        assertSame(expected, result);
        verify(repo).findByPatientIdOrderByAppointmentDateDesc(patientId);
    }

    @Test
    void appointmentsByDoctorIdAndDate_shouldReturnBookedScheduleOnly() {
        // Arrange
        Integer doctorId = 555;
        LocalDate date = LocalDate.of(2026, 5, 15);

        List<Appointment> expected = List.of(
                appt(1, doctorId, AppointmentStatus.BOOKED),
                appt(2, doctorId, AppointmentStatus.BOOKED)
        );

        when(repo.findByDoctorIdAndAppointmentDateAndStatusOrderByAppointmentTimeAsc(
                doctorId, date, AppointmentStatus.BOOKED
        )).thenReturn(expected);

        // Act
        List<Appointment> result = service.appointmentsByDoctorIdAndDate(doctorId, date);

        // Assert
        assertSame(expected, result);
        verify(repo).findByDoctorIdAndAppointmentDateAndStatusOrderByAppointmentTimeAsc(
                doctorId, date, AppointmentStatus.BOOKED
        );
    }

    @Test
    void updateRemarksByDoctor_shouldSetRemarksAndMarkCompleted() {
        // Arrange
        Integer apptId = 77;
        String doctorEmail = "doc@mail.com";
        Integer doctorId = 700;
        mockDoctorIdLookup(doctorEmail, doctorId);

        Appointment a = appt(10, doctorId, AppointmentStatus.BOOKED);
        when(repo.findById(apptId)).thenReturn(Optional.of(a));
        when(repo.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Appointment result = service.updateRemarksByDoctor(apptId, "Follow-up after 1 week", doctorEmail);

        // Assert
        assertEquals("Follow-up after 1 week", result.getRemarks());
        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());
        verify(repo).save(a);
    }

    @Test
    void updateRemarksByDoctor_shouldThrowIfDoctorNotOwner() {
        // Arrange
        Integer apptId = 78;
        String doctorEmail = "doc2@mail.com";
        Integer loggedInDoctorId = 800;
        mockDoctorIdLookup(doctorEmail, loggedInDoctorId);

        Appointment a = appt(10, 999, AppointmentStatus.BOOKED); // different doctorId
        when(repo.findById(apptId)).thenReturn(Optional.of(a));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateRemarksByDoctor(apptId, "Notes", doctorEmail));

        assertTrue(ex.getMessage().contains("You cannot update this appointment"));
        verify(repo, never()).save(any());
    }

    @Test
    void updateRemarksByDoctor_shouldThrowIfAppointmentNotFound() {
        // Arrange
        Integer apptId = 79;
        String doctorEmail = "doc3@mail.com";
        mockDoctorIdLookup(doctorEmail, 900);

        when(repo.findById(apptId)).thenReturn(Optional.empty());

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.updateRemarksByDoctor(apptId, "Notes", doctorEmail));

        assertTrue(ex.getMessage().contains("Appointment not found"));
        verify(repo, never()).save(any());
    }
}