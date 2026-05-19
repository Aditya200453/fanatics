package com.clinic.prescription;

import com.clinic.prescription.entity.Prescription;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.repository.PrescriptionMedicineRepository;
import com.clinic.prescription.repository.PrescriptionRepository;
import com.clinic.prescription.service.PrescriptionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PrescriptionMedicineRepository medicineRepository;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    // ---------- Helper method ----------
    private Prescription buildPrescription(Integer prescriptionId,
                                           Integer appointmentId,
                                           LocalDate prescriptionDate,
                                           String diagnosis,
                                           String notes) {
        Prescription p = new Prescription();
        // These setters are assumed based on your service usage.
        // If your entity names differ slightly, just adjust setters/getters.
        p.setPrescriptionId(prescriptionId);
        p.setAppointmentId(appointmentId);
        p.setPrescriptionDate(prescriptionDate);
        p.setDiagnosis(diagnosis);
        p.setNotes(notes);
        return p;
    }

    // ===========================
    // getAllPrescriptions
    // ===========================
    @Test
    @DisplayName("getAllPrescriptions: should return all prescriptions")
    void getAllPrescriptions_shouldReturnAll() {
        List<Prescription> mockList = List.of(
                buildPrescription(1, 101, LocalDate.now(), "D1", "N1"),
                buildPrescription(2, 102, LocalDate.now(), "D2", "N2")
        );

        when(prescriptionRepository.findAll()).thenReturn(mockList);

        List<Prescription> result = prescriptionService.getAllPrescriptions();

        assertEquals(2, result.size());
        assertSame(mockList, result);
        verify(prescriptionRepository, times(1)).findAll();
        verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
    }

    // ===========================
    // getPrescription
    // ===========================
    @Test
    @DisplayName("getPrescription: should return prescription when found")
    void getPrescription_shouldReturnWhenFound() {
        Prescription p = buildPrescription(1, 101, LocalDate.now(), "Dx", "Notes");

        when(prescriptionRepository.findById(1)).thenReturn(Optional.of(p));

        Prescription result = prescriptionService.getPrescription(1);

        assertSame(p, result);
        verify(prescriptionRepository).findById(1);
        verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
    }

    @Test
    @DisplayName("getPrescription: should throw PrescriptionNotFoundException when not found")
    void getPrescription_shouldThrowWhenNotFound() {
        when(prescriptionRepository.findById(99)).thenReturn(Optional.empty());

        PrescriptionNotFoundException ex = assertThrows(
                PrescriptionNotFoundException.class,
                () -> prescriptionService.getPrescription(99)
        );

        assertTrue(ex.getMessage().contains("99"));
        verify(prescriptionRepository).findById(99);
        verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
    }

    // ===========================
    // createPrescription
    // ===========================
    @Nested
    class CreatePrescriptionTests {

        @Test
        @DisplayName("createPrescription: should throw conflict when appointmentId is null")
        void createPrescription_shouldThrowWhenAppointmentIdNull() {
            Prescription p = buildPrescription(null, null, LocalDate.now(), "Dx", "Notes");

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.createPrescription(p)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("appointmentid"));
            verifyNoInteractions(prescriptionRepository, medicineRepository);
        }

        @Test
        @DisplayName("createPrescription: should throw conflict when prescriptionDate is null")
        void createPrescription_shouldThrowWhenPrescriptionDateNull() {
            Prescription p = buildPrescription(null, 101, null, "Dx", "Notes");

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.createPrescription(p)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("prescriptiondate"));
            verifyNoInteractions(prescriptionRepository, medicineRepository);
        }

        @Test
        @DisplayName("createPrescription: should throw conflict when prescription exists for appointmentId")
        void createPrescription_shouldThrowWhenExistsByAppointmentId() {
            Prescription p = buildPrescription(null, 101, LocalDate.now(), "Dx", "Notes");

            when(prescriptionRepository.existsByAppointmentId(101)).thenReturn(true);

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.createPrescription(p)
            );

            assertTrue(ex.getMessage().contains("101"));
            verify(prescriptionRepository).existsByAppointmentId(101);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("createPrescription: should save successfully when valid and not existing")
        void createPrescription_shouldSaveSuccessfully() {
            Prescription p = buildPrescription(null, 101, LocalDate.now(), "Dx", "Notes");
            Prescription saved = buildPrescription(1, 101, p.getPrescriptionDate(), "Dx", "Notes");

            when(prescriptionRepository.existsByAppointmentId(101)).thenReturn(false);
            when(prescriptionRepository.save(p)).thenReturn(saved);

            Prescription result = prescriptionService.createPrescription(p);

            assertEquals(1, result.getPrescriptionId());
            assertEquals(101, result.getAppointmentId());

            verify(prescriptionRepository).existsByAppointmentId(101);
            verify(prescriptionRepository).save(p);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }
    }

    // ===========================
    // updatePrescription
    // ===========================
    @Nested
    class UpdatePrescriptionTests {

        @Test
        @DisplayName("updatePrescription: should throw conflict when prescriptionId is null")
        void updatePrescription_shouldThrowWhenIdNull() {
            Prescription p = buildPrescription(null, 101, LocalDate.now(), "Dx", "Notes");

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.updatePrescription(p)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("prescriptionid"));
            verifyNoInteractions(prescriptionRepository, medicineRepository);
        }

        @Test
        @DisplayName("updatePrescription: should throw not found when prescriptionId does not exist")
        void updatePrescription_shouldThrowWhenNotExistsById() {
            Prescription p = buildPrescription(99, 101, LocalDate.now(), "Dx", "Notes");

            when(prescriptionRepository.existsById(99)).thenReturn(false);

            PrescriptionNotFoundException ex = assertThrows(
                    PrescriptionNotFoundException.class,
                    () -> prescriptionService.updatePrescription(p)
            );

            assertTrue(ex.getMessage().contains("99"));
            verify(prescriptionRepository).existsById(99);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("updatePrescription: should throw conflict when appointmentId is null")
        void updatePrescription_shouldThrowWhenAppointmentIdNull() {
            Prescription p = buildPrescription(1, null, LocalDate.now(), "Dx", "Notes");

            when(prescriptionRepository.existsById(1)).thenReturn(true);

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.updatePrescription(p)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("appointmentid"));
            verify(prescriptionRepository).existsById(1);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("updatePrescription: should throw conflict when prescriptionDate is null")
        void updatePrescription_shouldThrowWhenPrescriptionDateNull() {
            Prescription p = buildPrescription(1, 101, null, "Dx", "Notes");

            when(prescriptionRepository.existsById(1)).thenReturn(true);

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> prescriptionService.updatePrescription(p)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("prescriptiondate"));
            verify(prescriptionRepository).existsById(1);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("updatePrescription: should save successfully when valid")
        void updatePrescription_shouldSaveSuccessfully() {
            Prescription p = buildPrescription(1, 101, LocalDate.now(), "Dx", "Notes");

            when(prescriptionRepository.existsById(1)).thenReturn(true);
            when(prescriptionRepository.save(p)).thenReturn(p);

            Prescription result = prescriptionService.updatePrescription(p);

            assertSame(p, result);
            verify(prescriptionRepository).existsById(1);
            verify(prescriptionRepository).save(p);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }
    }

    // ===========================
    // patchPrescription
    // ===========================
    @Nested
    class PatchPrescriptionTests {

        @Test
        @DisplayName("patchPrescription: should update only non-null fields and save")
        void patchPrescription_shouldPatchNonNullFieldsOnly() {
            Prescription existing = buildPrescription(
                    1, 101, LocalDate.of(2026, 1, 1), "OldDx", "OldNotes"
            );

            // updates: change appointmentId + diagnosis, keep prescriptionDate null (should not override),
            // notes null (should not override)
            Prescription updates = buildPrescription(
                    null, 202, null, "NewDx", null
            );

            when(prescriptionRepository.findById(1)).thenReturn(Optional.of(existing));
            when(prescriptionRepository.save(any(Prescription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Prescription result = prescriptionService.patchPrescription(1, updates);

            assertEquals(202, result.getAppointmentId()); // updated
            assertEquals(LocalDate.of(2026, 1, 1), result.getPrescriptionDate()); // unchanged
            assertEquals("NewDx", result.getDiagnosis()); // updated
            assertEquals("OldNotes", result.getNotes()); // unchanged

            ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
            verify(prescriptionRepository).findById(1);
            verify(prescriptionRepository).save(captor.capture());
            assertSame(existing, captor.getValue()); // it saves the same existing object
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("patchPrescription: should throw not found when id not present")
        void patchPrescription_shouldThrowWhenNotFound() {
            when(prescriptionRepository.findById(55)).thenReturn(Optional.empty());

            assertThrows(
                    PrescriptionNotFoundException.class,
                    () -> prescriptionService.patchPrescription(55, new Prescription())
            );

            verify(prescriptionRepository).findById(55);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }
    }

    // ===========================
    // deletePrescription
    // ===========================
    @Nested
    class DeletePrescriptionTests {

        @Test
        @DisplayName("deletePrescription: should throw not found when prescription does not exist")
        void deletePrescription_shouldThrowWhenNotExists() {
            when(prescriptionRepository.existsById(99)).thenReturn(false);

            assertThrows(
                    PrescriptionNotFoundException.class,
                    () -> prescriptionService.deletePrescription(99)
            );

            verify(prescriptionRepository).existsById(99);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("deletePrescription: should delete medicines first then prescription")
        void deletePrescription_shouldDeleteChildrenThenParent() {
            when(prescriptionRepository.existsById(1)).thenReturn(true);

            prescriptionService.deletePrescription(1);

            InOrder inOrder = inOrder(medicineRepository, prescriptionRepository);

            inOrder.verify(prescriptionRepository).existsById(1);
            inOrder.verify(medicineRepository).deleteByPrescriptionId(1);
            inOrder.verify(prescriptionRepository).deleteById(1);

            verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
        }
    }

    // ===========================
    // getPrescriptionsByAppointment
    // ===========================
    @Test
    @DisplayName("getPrescriptionsByAppointment: should return prescriptions list")
    void getPrescriptionsByAppointment_shouldReturnList() {
        List<Prescription> list = List.of(
                buildPrescription(1, 500, LocalDate.now(), "Dx", "Notes")
        );

        when(prescriptionRepository.findByAppointmentId(500)).thenReturn(list);

        List<Prescription> result = prescriptionService.getPrescriptionsByAppointment(500);

        assertSame(list, result);
        verify(prescriptionRepository).findByAppointmentId(500);
        verifyNoMoreInteractions(prescriptionRepository);
        verifyNoInteractions(medicineRepository);
    }
}