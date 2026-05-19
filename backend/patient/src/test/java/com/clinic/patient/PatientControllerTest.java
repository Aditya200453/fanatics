package com.clinic.patient;

import com.clinic.patient.controller.PatientController;
import com.clinic.patient.entity.Patient;
import com.clinic.patient.repository.PatientRepository;
import com.clinic.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@TestPropertySource(properties = "app.internal-key=test-internal-key")
public class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @MockitoBean
    private PatientRepository patientRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    void signupPatient_success_returns200_and_callsAuthService_and_savesPatient() throws Exception {

        when(patientRepository.existsByEmail("ravi@clinic.com")).thenReturn(false);
        when(patientRepository.existsByPhone("9876543210")).thenReturn(false);

        when(restTemplate.postForEntity(
                eq("http://auth-service/auth/signup"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));

        String bodyJson = """
                {
                  "name": "Ravi Kumar",
                  "age": 28,
                  "dob": "1996-04-10",
                  "gender": "MALE",
                  "phone": "9876543210",
                  "email": "  RAVI@CLINIC.COM  ",
                  "password": "patient123",
                  "address": "Hyderabad"
                }
                """;

        mockMvc.perform(post("/patient/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Patient registered successfully"));

        verify(restTemplate, times(1)).postForEntity(
                eq("http://auth-service/auth/signup"),
                any(HttpEntity.class),
                eq(String.class)
        );

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientService, times(1)).save(captor.capture());

        Patient saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo("ravi@clinic.com");
        assertThat(saved.getPhone()).isEqualTo("9876543210");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getName()).isEqualTo("Ravi Kumar");
        assertThat(saved.getAddress()).isEqualTo("Hyderabad");
    }

    @Test
    void signupPatient_duplicateEmail_returns400_and_doesNotCallAuthOrSave() throws Exception {

        when(patientRepository.existsByEmail("ravi@clinic.com")).thenReturn(true);

        String bodyJson = """
                {
                  "name": "Ravi Kumar",
                  "age": 28,
                  "dob": "1996-04-10",
                  "gender": "MALE",
                  "phone": "9876543210",
                  "email": "ravi@clinic.com",
                  "password": "patient123",
                  "address": "Hyderabad"
                }
                """;

        mockMvc.perform(post("/patient/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));

        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
        verify(patientService, never()).save(any(Patient.class));
    }

    @Test
    void signupPatient_duplicatePhone_returns400_and_doesNotCallAuthOrSave() throws Exception {

        when(patientRepository.existsByEmail("ravi@clinic.com")).thenReturn(false);
        when(patientRepository.existsByPhone("9876543210")).thenReturn(true);

        String bodyJson = """
                {
                  "name": "Ravi Kumar",
                  "age": 28,
                  "dob": "1996-04-10",
                  "gender": "MALE",
                  "phone": "9876543210",
                  "email": "ravi@clinic.com",
                  "password": "patient123",
                  "address": "Hyderabad"
                }
                """;

        mockMvc.perform(post("/patient/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone already exists"));

        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
        verify(patientService, never()).save(any(Patient.class));
    }

    @Test
    void getMyProfile_missingHeader_returns401() throws Exception {
        mockMvc.perform(get("/patient/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_withHeader_returns200_patientJson() throws Exception {

        Patient p = new Patient();
        p.setPatientId(10);
        p.setName("Ravi Kumar");
        p.setEmail("ravi@clinic.com");
        p.setPhone("9876543210");
        p.setStatus("ACTIVE");

        when(patientService.getLoggedInPatient("ravi@clinic.com")).thenReturn(p);

        mockMvc.perform(get("/patient/me")
                        .header("X-User-Email", "  RAVI@CLINIC.COM "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(10))
                .andExpect(jsonPath("$.email").value("ravi@clinic.com"))
                .andExpect(jsonPath("$.name").value("Ravi Kumar"));
    }

    @Test
    void getPatientIdByEmail_wrongInternalKey_returns403() throws Exception {
        mockMvc.perform(get("/patient/internal/id")
                        .header("X-INTERNAL-KEY", "wrong-key")
                        .param("email", "ravi@clinic.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPatientIdByEmail_validInternalKey_returns200_id() throws Exception {

        Patient p = new Patient();
        p.setPatientId(7);
        p.setEmail("ravi@clinic.com");

        when(patientRepository.findByEmail("ravi@clinic.com")).thenReturn(Optional.of(p));

        mockMvc.perform(get("/patient/internal/id")
                        .header("X-INTERNAL-KEY", "test-internal-key")
                        .param("email", "  RAVI@CLINIC.COM "))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void getAllPatientsForStaff_rolePatient_returns403() throws Exception {
        mockMvc.perform(get("/patient/all")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllPatientsForStaff_roleStaff_returns200_list() throws Exception {

        Patient p1 = new Patient();
        p1.setPatientId(1);
        p1.setName("A");

        when(patientService.getAllPatients()).thenReturn(List.of(p1));

        mockMvc.perform(get("/patient/all")
                        .header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(1))
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void getPatient_success_returns200_patientJson() throws Exception {

        Patient p = new Patient();
        p.setPatientId(5);
        p.setName("Ravi");

        when(patientService.getOnePatient(5)).thenReturn(p);

        mockMvc.perform(get("/patient/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(5))
                .andExpect(jsonPath("$.name").value("Ravi"));
    }
}
