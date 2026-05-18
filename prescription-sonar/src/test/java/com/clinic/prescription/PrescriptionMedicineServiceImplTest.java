package com.clinic.prescription;

import com.clinic.prescription.entity.PrescriptionMedicine;
import com.clinic.prescription.exception.PrescriptionConflictException;
import com.clinic.prescription.exception.PrescriptionNotFoundException;
import com.clinic.prescription.repository.PrescriptionMedicineRepository;
import com.clinic.prescription.repository.PrescriptionRepository;
import com.clinic.prescription.service.PrescriptionMedicineServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionMedicineServiceImplTest {

    @Mock
    private PrescriptionMedicineRepository medicineRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private PrescriptionMedicineServiceImpl medicineService;

    // ---------- Helper ----------
    private PrescriptionMedicine buildMedicine(Integer medicineId, Integer prescriptionId, String medicineName) {
        PrescriptionMedicine m = new PrescriptionMedicine();
        // Adjust these setters if your entity uses different names / Lombok builder
        m.setPrescriptionMedicineId(medicineId);
        m.setPrescriptionId(prescriptionId);
        m.setMedicineName(medicineName);
        return m;
    }

    // ===========================
    // addMedicine
    // ===========================
    @Nested
    class AddMedicineTests {

        @Test
        @DisplayName("addMedicine: should throw PrescriptionNotFoundException when prescriptionId does not exist")
        void addMedicine_shouldThrowNotFound_whenPrescriptionDoesNotExist() {
            Integer prescriptionId = 99;
            PrescriptionMedicine medicine = buildMedicine(null, null, "Paracetamol");

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(false);

            PrescriptionNotFoundException ex = assertThrows(
                    PrescriptionNotFoundException.class,
                    () -> medicineService.addMedicine(prescriptionId, medicine)
            );

            assertTrue(ex.getMessage().contains("99"));
            verify(prescriptionRepository).existsById(prescriptionId);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("addMedicine: should throw PrescriptionConflictException when medicineName is null")
        void addMedicine_shouldThrowConflict_whenMedicineNameNull() {
            Integer prescriptionId = 1;
            PrescriptionMedicine medicine = buildMedicine(null, null, null);

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> medicineService.addMedicine(prescriptionId, medicine)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("medicinename"));
            verify(prescriptionRepository).existsById(prescriptionId);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("addMedicine: should throw PrescriptionConflictException when medicineName is blank")
        void addMedicine_shouldThrowConflict_whenMedicineNameBlank() {
            Integer prescriptionId = 1;
            PrescriptionMedicine medicine = buildMedicine(null, null, "   ");

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);

            PrescriptionConflictException ex = assertThrows(
                    PrescriptionConflictException.class,
                    () -> medicineService.addMedicine(prescriptionId, medicine)
            );

            assertTrue(ex.getMessage().toLowerCase().contains("medicinename"));
            verify(prescriptionRepository).existsById(prescriptionId);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("addMedicine: should set prescriptionId on medicine and save successfully")
        void addMedicine_shouldSetPrescriptionIdAndSave() {
            Integer prescriptionId = 10;

            PrescriptionMedicine input = buildMedicine(null, null, "Amoxicillin");
            PrescriptionMedicine saved = buildMedicine(101, prescriptionId, "Amoxicillin");

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);
            when(medicineRepository.save(any(PrescriptionMedicine.class))).thenReturn(saved);

            PrescriptionMedicine result = medicineService.addMedicine(prescriptionId, input);

            assertEquals(101, result.getPrescriptionMedicineId());
            assertEquals(prescriptionId, result.getPrescriptionId());
            assertEquals("Amoxicillin", result.getMedicineName());

            // Verify that prescriptionId was set BEFORE saving
            ArgumentCaptor<PrescriptionMedicine> captor = ArgumentCaptor.forClass(PrescriptionMedicine.class);
            verify(prescriptionRepository).existsById(prescriptionId);
            verify(medicineRepository).save(captor.capture());

            PrescriptionMedicine savedArg = captor.getValue();
            assertEquals(prescriptionId, savedArg.getPrescriptionId());
            assertEquals("Amoxicillin", savedArg.getMedicineName());

            verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
        }
    }

    // ===========================
    // getMedicines
    // ===========================
    @Nested
    class GetMedicinesTests {

        @Test
        @DisplayName("getMedicines: should throw PrescriptionNotFoundException when prescription does not exist")
        void getMedicines_shouldThrowNotFound_whenPrescriptionDoesNotExist() {
            Integer prescriptionId = 77;

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(false);

            assertThrows(
                    PrescriptionNotFoundException.class,
                    () -> medicineService.getMedicines(prescriptionId)
            );

            verify(prescriptionRepository).existsById(prescriptionId);
            verifyNoMoreInteractions(prescriptionRepository);
            verifyNoInteractions(medicineRepository);
        }

        @Test
        @DisplayName("getMedicines: should return list of medicines when prescription exists")
        void getMedicines_shouldReturnList_whenPrescriptionExists() {
            Integer prescriptionId = 5;

            List<PrescriptionMedicine> list = List.of(
                    buildMedicine(1, prescriptionId, "Med1"),
                    buildMedicine(2, prescriptionId, "Med2")
            );

            when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);
            when(medicineRepository.findByPrescriptionId(prescriptionId)).thenReturn(list);

            List<PrescriptionMedicine> result = medicineService.getMedicines(prescriptionId);

            assertEquals(2, result.size());
            assertSame(list, result);

            verify(prescriptionRepository).existsById(prescriptionId);
            verify(medicineRepository).findByPrescriptionId(prescriptionId);
            verifyNoMoreInteractions(prescriptionRepository, medicineRepository);
        }
    }

    // ===========================
    // deleteMedicine
    // ===========================
    @Test
    @DisplayName("deleteMedicine: should call repository deleteById")
    void deleteMedicine_shouldCallDeleteById() {
        Integer medicineId = 123;

        doNothing().when(medicineRepository).deleteById(medicineId);

        medicineService.deleteMedicine(medicineId);

        verify(medicineRepository).deleteById(medicineId);
        verifyNoInteractions(prescriptionRepository);
        verifyNoMoreInteractions(medicineRepository);
    }
}
