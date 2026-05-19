package com.clinic.doctor;

import com.clinic.doctor.controller.SpecialityController;
import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.service.SpecialityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpecialityController.class)
class SpecialityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpecialityService specialityService;

    // -------------------------------------------------
    // POST /speciality/  (ADMIN only)
    // -------------------------------------------------

    @Test
    void addSpeciality_nonAdmin_forbidden() throws Exception {
        String body = """
                { "name": "Cardiology", "description": "Heart related" }
                """;

        mockMvc.perform(post("/speciality/")
                        .header("X-User-Role", "STAFF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(specialityService, never()).addSpeciality(any());
    }

    @Test
    void addSpeciality_admin_success_returns200() throws Exception {
        Speciality created = new Speciality();
        created.setSpecialityId(1);
        created.setName("Cardiology");
        created.setDescription("Heart related");

        when(specialityService.addSpeciality(any(Speciality.class))).thenReturn(created);

        String body = """
                { "name": "Cardiology", "description": "Heart related" }
                """;

        mockMvc.perform(post("/speciality/")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialityId").value(1))
                .andExpect(jsonPath("$.name").value("Cardiology"))
                .andExpect(jsonPath("$.description").value("Heart related"));

        verify(specialityService, times(1)).addSpeciality(any(Speciality.class));
    }


    @Test
    void getAllSpecialities_invalidRole_forbidden() throws Exception {
        mockMvc.perform(get("/speciality/")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isForbidden());

        verify(specialityService, never()).getAllSpecialities();
    }

    @Test
    void getAllSpecialities_staff_success_returns200List() throws Exception {
        Speciality s1 = new Speciality();
        s1.setSpecialityId(1);
        s1.setName("Cardiology");

        when(specialityService.getAllSpecialities()).thenReturn(List.of(s1));

        mockMvc.perform(get("/speciality/")
                        .header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].specialityId").value(1))
                .andExpect(jsonPath("$[0].name").value("Cardiology"));

        verify(specialityService, times(1)).getAllSpecialities();
    }

    @Test
    void getAllSpecialities_patient_success_returns200() throws Exception {
        when(specialityService.getAllSpecialities()).thenReturn(List.of(new Speciality(), new Speciality()));

        mockMvc.perform(get("/speciality/")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(specialityService, times(1)).getAllSpecialities();
    }

    @Test
    void getAllSpecialities_admin_success_returns200() throws Exception {
        when(specialityService.getAllSpecialities()).thenReturn(List.of(new Speciality()));

        mockMvc.perform(get("/speciality/")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(specialityService, times(1)).getAllSpecialities();
    }

    @Test
    void getDoctorsBySpeciality_invalidRole_forbidden() throws Exception {
        mockMvc.perform(get("/speciality/1/doctors")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isForbidden());

        verify(specialityService, never()).getDoctorsBySpeciality(anyInt());
    }

    @Test
    void getDoctorsBySpeciality_staff_success_returns200List() throws Exception {
        Doctor d1 = new Doctor();
        d1.setDoctorId(1);
        d1.setName("Dr A");

        Doctor d2 = new Doctor();
        d2.setDoctorId(2);
        d2.setName("Dr B");

        when(specialityService.getDoctorsBySpeciality(1)).thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/speciality/1/doctors")
                        .header("X-User-Role", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorId").value(1))
                .andExpect(jsonPath("$[1].doctorId").value(2));

        verify(specialityService, times(1)).getDoctorsBySpeciality(1);
    }

    @Test
    void mapDoctor_patient_forbidden() throws Exception {
        String body = """
                { "specialityId": 1, "doctorId": 3 }
                """;

        mockMvc.perform(post("/speciality/map")
                        .header("X-User-Role", "PATIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(specialityService, never()).mapDoctorToSpeciality(anyInt(), anyInt());
    }

    @Test
    void mapDoctor_admin_success_returns200() throws Exception {
        SpecialityDoctorMap mapped = new SpecialityDoctorMap();
        mapped.setSpecialityId(1);
        mapped.setDoctorId(3);

        when(specialityService.mapDoctorToSpeciality(1, 3)).thenReturn(mapped);

        String body = """
                { "specialityId": 1, "doctorId": 3 }
                """;

        mockMvc.perform(post("/speciality/map")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialityId").value(1))
                .andExpect(jsonPath("$.doctorId").value(3));

        verify(specialityService, times(1)).mapDoctorToSpeciality(1, 3);
    }

    @Test
    void unmapDoctor_staff_success_returns200() throws Exception {
        mockMvc.perform(delete("/speciality/map")
                        .header("X-User-Role", "STAFF")
                        .param("specialityId", "1")
                        .param("doctorId", "3"))
                .andExpect(status().isOk());

        verify(specialityService, times(1)).removeDoctorFromSpeciality(1, 3);
    }

    @Test
    void deleteSpeciality_staff_forbidden() throws Exception {
        mockMvc.perform(delete("/speciality/5")
                        .header("X-User-Role", "STAFF"))
                .andExpect(status().isForbidden());

        verify(specialityService, never()).deleteSpeciality(anyInt());
    }

    @Test
    void deleteSpeciality_admin_success_returns200() throws Exception {
        mockMvc.perform(delete("/speciality/5")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());

        verify(specialityService, times(1)).deleteSpeciality(5);
    }
}