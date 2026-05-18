package com.clinic.doctor;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.exception.DoctorExsistsException;
import com.clinic.doctor.exception.DoctorNotFoundException;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.service.DoctorServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceImplTest {

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorServiceImpl doctorService;

    private Doctor doctor(Integer id, String email, String name) {
        Doctor d = new Doctor();
        d.setDoctorId(id);
        d.setEmail(email);
        d.setName(name);
        d.setExperience(5);
        d.setQualification("MBBS");
        d.setPhone("9999999999");
        d.setStatus("PENDING");
        return d;
    }

    // -------------------------
    // getAllDoctors
    // -------------------------

    @Test
    @DisplayName("getAllDoctors: returns list from repository")
    void getAllDoctors_returnsList() {
        when(doctorRepository.findAll()).thenReturn(List.of(
                doctor(1, "a@a.com", "A"),
                doctor(2, "b@b.com", "B")
        ));

        List<Doctor> result = doctorService.getAllDoctors();

        assertThat(result).hasSize(2);
        verify(doctorRepository, times(1)).findAll();
    }

    // -------------------------
    // getDoctor
    // -------------------------

    @Test
    @DisplayName("getDoctor: returns doctor when id exists")
    void getDoctor_success() {
        Doctor existing = doctor(10, "dr@clinic.com", "Dr X");
        when(doctorRepository.findById(10)).thenReturn(Optional.of(existing));

        Doctor result = doctorService.getDoctor(10);

        assertThat(result.getDoctorId()).isEqualTo(10);
        assertThat(result.getEmail()).isEqualTo("dr@clinic.com");
        verify(doctorRepository).findById(10);
    }

    @Test
    @DisplayName("getDoctor: throws DoctorNotFoundException when id missing")
    void getDoctor_notFound() {
        when(doctorRepository.findById(99)).thenReturn(Optional.empty());

        DoctorNotFoundException ex =
                assertThrows(DoctorNotFoundException.class, () -> doctorService.getDoctor(99));

        assertThat(ex.getMessage()).isEqualTo("Doctor with Id 99 not found");
        verify(doctorRepository).findById(99);
    }

    // -------------------------
    // saveDoctor
    // -------------------------

    @Test
    @DisplayName("saveDoctor: throws DoctorExsistsException when email already exists")
    void saveDoctor_duplicateEmail_throws() {
        Doctor input = doctor(null, "dup@clinic.com", "Dup");
        when(doctorRepository.existsByEmail("dup@clinic.com")).thenReturn(true);

        DoctorExsistsException ex =
                assertThrows(DoctorExsistsException.class, () -> doctorService.saveDoctor(input));

        assertThat(ex.getMessage()).contains("already exists");
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveDoctor: saves doctor when email not exists")
    void saveDoctor_success() {
        Doctor input = doctor(null, "new@clinic.com", "New");
        when(doctorRepository.existsByEmail("new@clinic.com")).thenReturn(false);
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        Doctor saved = doctorService.saveDoctor(input);

        assertThat(saved.getEmail()).isEqualTo("new@clinic.com");
        verify(doctorRepository).existsByEmail("new@clinic.com");
        verify(doctorRepository).save(any(Doctor.class));
    }

    // -------------------------
    // updateDoctor
    // -------------------------

    @Test
    @DisplayName("updateDoctor: throws DoctorNotFoundException when doctor not found")
    void updateDoctor_notFound_throws() {
        Doctor incoming = doctor(50, "x@x.com", "NewName");
        when(doctorRepository.findById(50)).thenReturn(Optional.empty());

        DoctorNotFoundException ex =
                assertThrows(DoctorNotFoundException.class, () -> doctorService.updateDoctor(incoming));

        assertThat(ex.getMessage()).isEqualTo("Doctor with Id 50 not found");
        verify(doctorRepository).findById(50);
        verify(doctorRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDoctor: updates fields and saves existing doctor")
    void updateDoctor_success_updatesAndSaves() {
        Doctor existing = doctor(5, "dr@clinic.com", "Old");
        Doctor incoming = doctor(5, "dr@clinic.com", "New");
        incoming.setExperience(9);
        incoming.setQualification("MD");
        incoming.setPhone("8888888888");
        incoming.setStatus("ACTIVE");

        when(doctorRepository.findById(5)).thenReturn(Optional.of(existing));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        Doctor updated = doctorService.updateDoctor(incoming);

        // verify returned object fields
        assertThat(updated.getDoctorId()).isEqualTo(5);
        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getExperience()).isEqualTo(9);
        assertThat(updated.getQualification()).isEqualTo("MD");
        assertThat(updated.getPhone()).isEqualTo("8888888888");
        assertThat(updated.getStatus()).isEqualTo("ACTIVE");

        // verify saved entity actually had updates applied
        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository).save(captor.capture());
        Doctor savedEntity = captor.getValue();

        assertThat(savedEntity.getName()).isEqualTo("New");
        assertThat(savedEntity.getExperience()).isEqualTo(9);
        assertThat(savedEntity.getQualification()).isEqualTo("MD");
        assertThat(savedEntity.getPhone()).isEqualTo("8888888888");
        assertThat(savedEntity.getStatus()).isEqualTo("ACTIVE");
    }

    // -------------------------
    // deleteDoctor
    // -------------------------

    @Test
    @DisplayName("deleteDoctor: throws DoctorNotFoundException when id doesn't exist")
    void deleteDoctor_notFound_throws() {
        when(doctorRepository.existsById(77)).thenReturn(false);

        DoctorNotFoundException ex =
                assertThrows(DoctorNotFoundException.class, () -> doctorService.deleteDoctor(77));

        assertThat(ex.getMessage()).isEqualTo("Doctor with Id 77 not found");
        verify(doctorRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("deleteDoctor: deletes when id exists")
    void deleteDoctor_success() {
        when(doctorRepository.existsById(10)).thenReturn(true);

        doctorService.deleteDoctor(10);

        verify(doctorRepository).deleteById(10);
    }
}
