package com.clinic.doctor;

import com.clinic.doctor.controller.DoctorController;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@TestPropertySource(properties = "app.internal-key=test-internal-key")
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private DoctorRepository doctorRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    // -------------------------
    // /doctor/signup
    // -------------------------

    @Test
    void signupDoctor_success_returns200() throws Exception {
        when(doctorRepository.existsByEmail("dr@clinic.com")).thenReturn(false);

        String body = """
                {
                  "name": "Dr John",
                  "experience": 8,
                  "qualification": "MBBS",
                  "phone": "9999999999",
                  "email": "  DR@CLINIC.COM ",
                  "password": "doctor123"
                }
                """;

        mockMvc.perform(post("/doctor/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Doctor signup submitted ✅ (waiting for approval)"));

        verify(doctorService, times(1)).saveDoctor(any(Doctor.class));
    }

    @Test
    void signupDoctor_duplicateEmail_returns400() throws Exception {
        when(doctorRepository.existsByEmail("dr@clinic.com")).thenReturn(true);

        String body = """
                {
                  "name": "Dr John",
                  "experience": 8,
                  "qualification": "MBBS",
                  "phone": "9999999999",
                  "email": "dr@clinic.com",
                  "password": "doctor123"
                }
                """;

        mockMvc.perform(post("/doctor/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Doctor already exists"));

        verify(doctorService, never()).saveDoctor(any());
    }

    @Test
    void signupDoctor_shortPassword_returns400() throws Exception {
        when(doctorRepository.existsByEmail("dr@clinic.com")).thenReturn(false);

        String body = """
                {
                  "name": "Dr John",
                  "experience": 8,
                  "qualification": "MBBS",
                  "phone": "9999999999",
                  "email": "dr@clinic.com",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/doctor/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 6 characters"));

        verify(doctorService, never()).saveDoctor(any());
    }

    // -------------------------
    // /doctor/admin/doctors and /doctor/all (role header)
    // -------------------------

    @Test
    void getAllDoctors_adminDoctors_roleMissing_returns403() throws Exception {
        mockMvc.perform(get("/doctor/admin/doctors"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDoctors_adminDoctors_rolePatient_returns403() throws Exception {
        mockMvc.perform(get("/doctor/admin/doctors").header("X-User-Role", "PATIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllDoctors_adminDoctors_roleAdmin_returns200() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(1);
        d.setEmail("a@a.com");
        when(doctorRepository.findAll()).thenReturn(List.of(d));

        mockMvc.perform(get("/doctor/admin/doctors").header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorId").value(1));
    }

    @Test
    void getAllDoctors_all_roleStaff_returns200() throws Exception {
        when(doctorRepository.findAll()).thenReturn(List.of(new Doctor()));

        mockMvc.perform(get("/doctor/all").header("X-User-Role", "STAFF"))
                .andExpect(status().isOk());
    }

    // -------------------------
    // /doctor/admin/approve/{id}
    // -------------------------

    @Test
    void approveDoctor_alreadyActive_returns200() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(3);
        d.setStatus("ACTIVE");
        when(doctorService.getDoctor(3)).thenReturn(d);

        mockMvc.perform(post("/doctor/admin/approve/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor already approved ✅"));

        verify(doctorService, never()).updateDoctor(any());
        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void approveDoctor_tempPasswordMissing_returns400() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(3);
        d.setStatus("PENDING");
        d.setTempPassword("   ");
        when(doctorService.getDoctor(3)).thenReturn(d);

        mockMvc.perform(post("/doctor/admin/approve/3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Doctor password missing. Please re-register doctor."));

        verify(doctorService, never()).updateDoctor(any());
    }

    @Test
    void approveDoctor_success_callsAuthInternal_andActivates() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(3);
        d.setEmail("dr@clinic.com");
        d.setStatus("PENDING");
        d.setTempPassword("doctor123");
        when(doctorService.getDoctor(3)).thenReturn(d);

        when(restTemplate.postForEntity(
                eq("http://auth-service/auth/internal/doctor"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        mockMvc.perform(post("/doctor/admin/approve/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor approved and login enabled ✅"));

        // Ensure updateDoctor called with ACTIVE and tempPassword cleared
        verify(doctorService, times(1)).updateDoctor(argThat(updated ->
                "ACTIVE".equalsIgnoreCase(updated.getStatus()) && updated.getTempPassword() == null
        ));
    }

    @Test
    void approveDoctor_authServiceFails_stillActivates() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(3);
        d.setEmail("dr@clinic.com");
        d.setStatus("PENDING");
        d.setTempPassword("doctor123");
        when(doctorService.getDoctor(3)).thenReturn(d);

        // simulate 409/400 from auth-service
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(HttpClientErrorException.Conflict.create(
                        HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], null));

        mockMvc.perform(post("/doctor/admin/approve/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Doctor approved and login enabled ✅"));

        verify(doctorService, times(1)).updateDoctor(any(Doctor.class));
    }

    // -------------------------
    // /doctor/me
    // -------------------------

    @Test
    void getMyProfile_missingHeader_returns401() throws Exception {
        mockMvc.perform(get("/doctor/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_success_returns200() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(3);
        d.setEmail("dr@clinic.com");

        when(doctorRepository.findByEmail("dr@clinic.com")).thenReturn(Optional.of(d));

        mockMvc.perform(get("/doctor/me").header("X-User-Email", " DR@CLINIC.COM "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorId").value(3));
    }

    // -------------------------
    // /doctor/internal/id
    // -------------------------

    @Test
    void getDoctorIdByEmail_wrongKey_returns403() throws Exception {
        mockMvc.perform(get("/doctor/internal/id")
                        .header("X-INTERNAL-KEY", "wrong")
                        .param("email", "dr@clinic.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDoctorIdByEmail_validKey_returns200() throws Exception {
        Doctor d = new Doctor();
        d.setDoctorId(7);
        when(doctorRepository.findByEmail("dr@clinic.com")).thenReturn(Optional.of(d));

        mockMvc.perform(get("/doctor/internal/id")
                        .header("X-INTERNAL-KEY", "test-internal-key")
                        .param("email", " DR@CLINIC.COM "))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    // -------------------------
    // /doctor/public/active
    // -------------------------

    @Test
    void getActiveDoctors_returns200_list() throws Exception {
        when(doctorRepository.findByStatus("ACTIVE")).thenReturn(List.of(new Doctor(), new Doctor()));

        mockMvc.perform(get("/doctor/public/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}