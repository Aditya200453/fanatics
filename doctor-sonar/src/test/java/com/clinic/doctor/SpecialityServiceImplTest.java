package com.clinic.doctor;

import com.clinic.doctor.entity.Doctor;
import com.clinic.doctor.entity.Speciality;
import com.clinic.doctor.entity.SpecialityDoctorMap;
import com.clinic.doctor.entity.SpecialityDoctorMapId;
import com.clinic.doctor.exception.DoctorNotFoundException;
import com.clinic.doctor.exception.SpecialityNotFoundException;
import com.clinic.doctor.repository.DoctorRepository;
import com.clinic.doctor.repository.SpecialityDoctorMapRepository;
import com.clinic.doctor.repository.SpecialityRepository;
import com.clinic.doctor.service.SpecialityServiceImpl;
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
class SpecialityServiceImplTest {

    @Mock
    private SpecialityRepository specialityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpecialityDoctorMapRepository mapRepository;

    @InjectMocks
    private SpecialityServiceImpl specialityService;

    // -------------------------
    // Helpers
    // -------------------------
    private Speciality speciality(Integer id, String name) {
        Speciality s = new Speciality();
        s.setSpecialityId(id);
        s.setName(name);
        s.setDescription(name + " desc");
        return s;
    }

    private Doctor doctor(Integer id, String name) {
        Doctor d = new Doctor();
        d.setDoctorId(id);
        d.setName(name);
        d.setEmail(name.toLowerCase() + "@clinic.com");
        d.setStatus("ACTIVE");
        return d;
    }

    private SpecialityDoctorMap mapping(Integer specialityId, Integer doctorId) {
        SpecialityDoctorMap m = new SpecialityDoctorMap();
        m.setSpecialityId(specialityId);
        m.setDoctorId(doctorId);
        return m;
    }

    // -------------------------
    // addSpeciality
    // -------------------------

    @Test
    @DisplayName("addSpeciality: saves and returns speciality")
    void addSpeciality_success() {
        Speciality input = speciality(null, "Cardiology");
        Speciality saved = speciality(1, "Cardiology");

        when(specialityRepository.save(any(Speciality.class))).thenReturn(saved);

        Speciality result = specialityService.addSpeciality(input);

        assertThat(result.getSpecialityId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Cardiology");
        verify(specialityRepository, times(1)).save(any(Speciality.class));
    }

    // -------------------------
    // getAllSpecialities
    // -------------------------

    @Test
    @DisplayName("getAllSpecialities: returns repository list")
    void getAllSpecialities_success() {
        when(specialityRepository.findAll()).thenReturn(List.of(
                speciality(1, "Cardiology"),
                speciality(2, "Dermatology")
        ));

        List<Speciality> list = specialityService.getAllSpecialities();

        assertThat(list).hasSize(2);
        verify(specialityRepository, times(1)).findAll();
    }

    // -------------------------
    // getSpeciality
    // -------------------------

    @Test
    @DisplayName("getSpeciality: returns speciality when found")
    void getSpeciality_success() {
        when(specialityRepository.findById(1)).thenReturn(Optional.of(speciality(1, "Cardiology")));

        Speciality result = specialityService.getSpeciality(1);

        assertThat(result.getSpecialityId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Cardiology");
        verify(specialityRepository).findById(1);
    }

    @Test
    @DisplayName("getSpeciality: throws SpecialityNotFoundException when missing")
    void getSpeciality_notFound() {
        when(specialityRepository.findById(99)).thenReturn(Optional.empty());

        SpecialityNotFoundException ex =
                assertThrows(SpecialityNotFoundException.class, () -> specialityService.getSpeciality(99));

        assertThat(ex.getMessage()).isEqualTo("Speciality with Id 99 not found");
        verify(specialityRepository).findById(99);
    }

    // -------------------------
    // mapDoctorToSpeciality
    // -------------------------

    @Test
    @DisplayName("mapDoctorToSpeciality: throws if speciality not found")
    void mapDoctorToSpeciality_specialityNotFound() {
        when(specialityRepository.findById(1)).thenReturn(Optional.empty());

        SpecialityNotFoundException ex =
                assertThrows(SpecialityNotFoundException.class,
                        () -> specialityService.mapDoctorToSpeciality(1, 10));

        assertThat(ex.getMessage()).isEqualTo("Speciality with Id 1 not found");
        verify(specialityRepository).findById(1);
        verify(doctorRepository, never()).findById(anyInt());
        verify(mapRepository, never()).save(any());
    }

    @Test
    @DisplayName("mapDoctorToSpeciality: throws if doctor not found")
    void mapDoctorToSpeciality_doctorNotFound() {
        when(specialityRepository.findById(1)).thenReturn(Optional.of(speciality(1, "Cardiology")));
        when(doctorRepository.findById(10)).thenReturn(Optional.empty());

        DoctorNotFoundException ex =
                assertThrows(DoctorNotFoundException.class,
                        () -> specialityService.mapDoctorToSpeciality(1, 10));

        assertThat(ex.getMessage()).isEqualTo("Doctor with Id 10 not found");
        verify(doctorRepository).findById(10);
        verify(mapRepository, never()).save(any());
    }

    @Test
    @DisplayName("mapDoctorToSpeciality: if mapping exists, returns mapping without saving")
    void mapDoctorToSpeciality_alreadyMapped_returnsExisting_noSave() {
        when(specialityRepository.findById(1)).thenReturn(Optional.of(speciality(1, "Cardiology")));
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doctor(10, "Dr A")));
        when(mapRepository.existsById(any(SpecialityDoctorMapId.class))).thenReturn(true);

        SpecialityDoctorMap result = specialityService.mapDoctorToSpeciality(1, 10);

        assertThat(result.getSpecialityId()).isEqualTo(1);
        assertThat(result.getDoctorId()).isEqualTo(10);
        verify(mapRepository, never()).save(any());
    }

    @Test
    @DisplayName("mapDoctorToSpeciality: saves mapping when not exists")
    void mapDoctorToSpeciality_newMapping_saves() {
        when(specialityRepository.findById(1)).thenReturn(Optional.of(speciality(1, "Cardiology")));
        when(doctorRepository.findById(10)).thenReturn(Optional.of(doctor(10, "Dr A")));
        when(mapRepository.existsById(any(SpecialityDoctorMapId.class))).thenReturn(false);

        when(mapRepository.save(any(SpecialityDoctorMap.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SpecialityDoctorMap result = specialityService.mapDoctorToSpeciality(1, 10);

        assertThat(result.getSpecialityId()).isEqualTo(1);
        assertThat(result.getDoctorId()).isEqualTo(10);

        ArgumentCaptor<SpecialityDoctorMap> captor = ArgumentCaptor.forClass(SpecialityDoctorMap.class);
        verify(mapRepository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getSpecialityId()).isEqualTo(1);
        assertThat(captor.getValue().getDoctorId()).isEqualTo(10);
    }

    // -------------------------
    // removeDoctorFromSpeciality
    // -------------------------

    @Test
    @DisplayName("removeDoctorFromSpeciality: deletes mapping by id")
    void removeDoctorFromSpeciality_deletes() {
        specialityService.removeDoctorFromSpeciality(5, 7);

        verify(mapRepository, times(1)).deleteById(any(SpecialityDoctorMapId.class));
        verifyNoMoreInteractions(mapRepository);
    }

    // -------------------------
    // getDoctorsBySpeciality
    // -------------------------

    @Test
    @DisplayName("getDoctorsBySpeciality: throws if speciality not found")
    void getDoctorsBySpeciality_specialityNotFound() {
        when(specialityRepository.findById(9)).thenReturn(Optional.empty());

        SpecialityNotFoundException ex =
                assertThrows(SpecialityNotFoundException.class,
                        () -> specialityService.getDoctorsBySpeciality(9));

        assertThat(ex.getMessage()).isEqualTo("Speciality with Id 9 not found");
        verify(specialityRepository).findById(9);
        verify(mapRepository, never()).findBySpecialityId(anyInt());
        verify(doctorRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("getDoctorsBySpeciality: returns doctors by mapping doctorIds")
    void getDoctorsBySpeciality_success() {
        when(specialityRepository.findById(1)).thenReturn(Optional.of(speciality(1, "Cardiology")));

        List<SpecialityDoctorMap> mappings = List.of(
                mapping(1, 10),
                mapping(1, 20)
        );
        when(mapRepository.findBySpecialityId(1)).thenReturn(mappings);

        List<Doctor> doctors = List.of(doctor(10, "Dr A"), doctor(20, "Dr B"));
        when(doctorRepository.findAllById(anyList())).thenReturn(doctors);

        List<Doctor> result = specialityService.getDoctorsBySpeciality(1);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDoctorId()).isEqualTo(10);
        assertThat(result.get(1).getDoctorId()).isEqualTo(20);

        // verify calls
        verify(specialityRepository).findById(1);
        verify(mapRepository).findBySpecialityId(1);
        verify(doctorRepository).findAllById(anyList());
    }
}