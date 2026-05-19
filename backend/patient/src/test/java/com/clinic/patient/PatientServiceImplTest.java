package com.clinic.patient;

import com.clinic.patient.entity.Patient;
import com.clinic.patient.exception.PatientExistsException;
import com.clinic.patient.exception.PatientNotFoundException;
import com.clinic.patient.repository.PatientRepository;
import com.clinic.patient.service.PatientServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    // -------------------------
    // Helpers
    // -------------------------
    private Patient newPatientNoId(String name, String phone, String email, String status) {
        Patient p = new Patient();
        p.setPatientId(null);
        p.setName(name);
        p.setPhone(phone);
        p.setEmail(email);
        p.setStatus(status);
        return p;
    }

    private Patient existingPatient(Integer id, String phone, String email, String status) {
        Patient p = new Patient();
        p.setPatientId(id);
        p.setName("Existing");
        p.setPhone(phone);
        p.setEmail(email);
        p.setStatus(status);
        return p;
    }

    // -------------------------
    // getAllPatients
    // -------------------------
    @Test
    @DisplayName("getAllPatients: returns repository list")
    void getAllPatients_returnsList() {
        when(patientRepository.findAll()).thenReturn(List.of(existingPatient(1, "111", "a@a.com", "ACTIVE")));

        List<Patient> result = patientService.getAllPatients();

        assertThat(result).hasSize(1);
        verify(patientRepository, times(1)).findAll();
    }

    // -------------------------
    // getOnePatient
    // -------------------------
    @Test
    @DisplayName("getOnePatient: success when id exists")
    void getOnePatient_success() {
        Patient existing = existingPatient(10, "999", "x@x.com", "ACTIVE");
        when(patientRepository.findById(10)).thenReturn(Optional.of(existing));

        Patient result = patientService.getOnePatient(10);

        assertThat(result.getPatientId()).isEqualTo(10);
        verify(patientRepository).findById(10);
    }

    @Test
    @DisplayName("getOnePatient: throws PatientNotFoundException when id not found")
    void getOnePatient_notFound() {
        when(patientRepository.findById(99)).thenReturn(Optional.empty());

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class, () -> patientService.getOnePatient(99));

        assertThat(ex.getMessage()).contains("Patient with Id 99 not found");
        verify(patientRepository).findById(99);
    }

    // -------------------------
    // save
    // -------------------------
    @Test
    @DisplayName("save: throws PatientExistsException when patientId is provided")
    void save_withIdProvided_throws() {
        Patient p = new Patient();
        p.setPatientId(1);
        p.setPhone("999");
        p.setEmail("x@x.com");

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.save(p));

        assertThat(ex.getMessage()).contains("PatientId must not be provided");
        verify(patientRepository, never()).save(any());
        verify(patientRepository, never()).existsByPhone(anyString());
        verify(patientRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("save: throws PatientExistsException when phone already exists")
    void save_duplicatePhone_throws() {
        Patient p = newPatientNoId("N", "999", "x@x.com", "ACTIVE");
        when(patientRepository.existsByPhone("999")).thenReturn(true);

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.save(p));

        assertThat(ex.getMessage()).contains("Phone already exists");
        verify(patientRepository).existsByPhone("999");
        verify(patientRepository, never()).existsByEmail(anyString());
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: throws PatientExistsException when email already exists")
    void save_duplicateEmail_throws() {
        Patient p = newPatientNoId("N", "999", "x@x.com", "ACTIVE");
        when(patientRepository.existsByPhone("999")).thenReturn(false);
        when(patientRepository.existsByEmail("x@x.com")).thenReturn(true);

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.save(p));

        assertThat(ex.getMessage()).contains("Email already exists");
        verify(patientRepository).existsByPhone("999");
        verify(patientRepository).existsByEmail("x@x.com");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("save: sets default ACTIVE when status is null/blank and saves")
    void save_defaultStatus_setsActive() {
        Patient p = newPatientNoId("N", "999", "x@x.com", "   "); // blank status
        when(patientRepository.existsByPhone("999")).thenReturn(false);
        when(patientRepository.existsByEmail("x@x.com")).thenReturn(false);

        // return same patient (simulating JPA save)
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient saved = patientService.save(p);

        assertThat(saved.getStatus()).isEqualTo("ACTIVE");

        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);
        verify(patientRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("ACTIVE");
    }

    // -------------------------
    // update
    // -------------------------
    @Test
    @DisplayName("update: throws PatientNotFoundException when patientId is null")
    void update_withoutId_throws() {
        Patient p = new Patient();
        p.setPatientId(null);

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class, () -> patientService.update(p));

        assertThat(ex.getMessage()).contains("PatientId is required for update");
        verify(patientRepository, never()).findById(anyInt());
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: throws PatientNotFoundException when patient not found in DB")
    void update_notFound_throws() {
        Patient p = new Patient();
        p.setPatientId(77);

        when(patientRepository.findById(77)).thenReturn(Optional.empty());

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class, () -> patientService.update(p));

        assertThat(ex.getMessage()).contains("Patient not found");
        verify(patientRepository).findById(77);
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: throws PatientExistsException when phone duplicates another patient")
    void update_duplicatePhone_throws() {
        Patient incoming = new Patient();
        incoming.setPatientId(5);
        incoming.setPhone("999");
        incoming.setEmail("new@x.com");

        Patient existing = existingPatient(5, "111", "old@x.com", "ACTIVE");
        when(patientRepository.findById(5)).thenReturn(Optional.of(existing));
        when(patientRepository.existsByPhoneAndPatientIdNot("999", 5)).thenReturn(true);

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.update(incoming));

        assertThat(ex.getMessage()).contains("Phone already exists");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: throws PatientExistsException when email duplicates another patient")
    void update_duplicateEmail_throws() {
        Patient incoming = new Patient();
        incoming.setPatientId(5);
        incoming.setPhone("222");
        incoming.setEmail("dup@x.com");

        Patient existing = existingPatient(5, "111", "old@x.com", "ACTIVE");
        when(patientRepository.findById(5)).thenReturn(Optional.of(existing));
        when(patientRepository.existsByPhoneAndPatientIdNot("222", 5)).thenReturn(false);
        when(patientRepository.existsByEmailAndPatientIdNot("dup@x.com", 5)).thenReturn(true);

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.update(incoming));

        assertThat(ex.getMessage()).contains("Email already exists");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("update: success updates fields and defaults status ACTIVE if null")
    void update_success_updatesFields() {
        Patient incoming = new Patient();
        incoming.setPatientId(5);
        incoming.setName("NewName");
        incoming.setAge(30);
        incoming.setGender("MALE");
        incoming.setPhone("222");
        incoming.setEmail("new@x.com");
        incoming.setAddress("Hyd");
        incoming.setStatus(null); // should default to ACTIVE

        Patient existing = existingPatient(5, "111", "old@x.com", "INACTIVE");

        when(patientRepository.findById(5)).thenReturn(Optional.of(existing));
        when(patientRepository.existsByPhoneAndPatientIdNot("222", 5)).thenReturn(false);
        when(patientRepository.existsByEmailAndPatientIdNot("new@x.com", 5)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient updated = patientService.update(incoming);

        assertThat(updated.getPatientId()).isEqualTo(5);
        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getAge()).isEqualTo(30);
        assertThat(updated.getGender()).isEqualTo("MALE");
        assertThat(updated.getPhone()).isEqualTo("222");
        assertThat(updated.getEmail()).isEqualTo("new@x.com");
        assertThat(updated.getAddress()).isEqualTo("Hyd");
        assertThat(updated.getStatus()).isEqualTo("ACTIVE");

        verify(patientRepository).save(any(Patient.class));
    }

    // -------------------------
    // delete
    // -------------------------
    @Test
    @DisplayName("delete: throws PatientNotFoundException when id does not exist")
    void delete_notFound_throws() {
        when(patientRepository.existsById(100)).thenReturn(false);

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class, () -> patientService.delete(100));

        assertThat(ex.getMessage()).contains("Patient not found");
        verify(patientRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("delete: deletes when id exists")
    void delete_success() {
        when(patientRepository.existsById(10)).thenReturn(true);

        patientService.delete(10);

        verify(patientRepository).deleteById(10);
    }

    // -------------------------
    // partialUpdate
    // -------------------------
    @Test
    @DisplayName("partialUpdate: throws PatientNotFoundException when target patient not found")
    void partialUpdate_notFound_throws() {
        when(patientRepository.findById(55)).thenReturn(Optional.empty());

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class,
                        () -> patientService.partialUpdate(55, new Patient()));

        assertThat(ex.getMessage()).contains("Patient not found");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdate: throws PatientExistsException when phone duplicates another patient")
    void partialUpdate_duplicatePhone_throws() {
        Patient target = existingPatient(1, "111", "old@x.com", "ACTIVE");
        when(patientRepository.findById(1)).thenReturn(Optional.of(target));
        when(patientRepository.existsByPhoneAndPatientIdNot("999", 1)).thenReturn(true);

        Patient patch = new Patient();
        patch.setPhone("999");

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.partialUpdate(1, patch));

        assertThat(ex.getMessage()).contains("Phone already exists");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdate: throws PatientExistsException when email duplicates another patient")
    void partialUpdate_duplicateEmail_throws() {
        Patient target = existingPatient(1, "111", "old@x.com", "ACTIVE");
        when(patientRepository.findById(1)).thenReturn(Optional.of(target));
        when(patientRepository.existsByEmailAndPatientIdNot("dup@x.com", 1)).thenReturn(true);

        Patient patch = new Patient();
        patch.setEmail("dup@x.com");

        PatientExistsException ex =
                assertThrows(PatientExistsException.class, () -> patientService.partialUpdate(1, patch));

        assertThat(ex.getMessage()).contains("Email already exists");
        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("partialUpdate: success updates only provided fields and saves")
    void partialUpdate_success_updatesOnlyProvidedFields() {
        Patient target = existingPatient(1, "111", "old@x.com", "ACTIVE");
        target.setAddress("OldAddr");
        target.setName("OldName");
        target.setAge(20);

        when(patientRepository.findById(1)).thenReturn(Optional.of(target));
        when(patientRepository.existsByPhoneAndPatientIdNot("222", 1)).thenReturn(false);
        when(patientRepository.existsByEmailAndPatientIdNot("new@x.com", 1)).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient patch = new Patient();
        patch.setName("NewName");
        patch.setPhone("222");
        patch.setEmail("new@x.com");
        patch.setAddress("NewAddr");
        patch.setStatus("INACTIVE");

        Patient updated = patientService.partialUpdate(1, patch);

        assertThat(updated.getName()).isEqualTo("NewName");
        assertThat(updated.getPhone()).isEqualTo("222");
        assertThat(updated.getEmail()).isEqualTo("new@x.com");
        assertThat(updated.getAddress()).isEqualTo("NewAddr");
        assertThat(updated.getStatus()).isEqualTo("INACTIVE");
        assertThat(updated.getAge()).isEqualTo(20); // unchanged

        verify(patientRepository).save(any(Patient.class));
    }

    // -------------------------
    // getLoggedInPatient
    // -------------------------
    @Test
    @DisplayName("getLoggedInPatient: returns patient when email exists")
    void getLoggedInPatient_success() {
        Patient p = existingPatient(2, "111", "a@a.com", "ACTIVE");
        when(patientRepository.findByEmail("a@a.com")).thenReturn(Optional.of(p));

        Patient result = patientService.getLoggedInPatient("a@a.com");

        assertThat(result.getPatientId()).isEqualTo(2);
        verify(patientRepository).findByEmail("a@a.com");
    }

    @Test
    @DisplayName("getLoggedInPatient: throws PatientNotFoundException when email not found")
    void getLoggedInPatient_notFound() {
        when(patientRepository.findByEmail("missing@x.com")).thenReturn(Optional.empty());

        PatientNotFoundException ex =
                assertThrows(PatientNotFoundException.class,
                        () -> patientService.getLoggedInPatient("missing@x.com"));

        assertThat(ex.getMessage()).contains("Patient not found for email");
        verify(patientRepository).findByEmail("missing@x.com");
    }
}
