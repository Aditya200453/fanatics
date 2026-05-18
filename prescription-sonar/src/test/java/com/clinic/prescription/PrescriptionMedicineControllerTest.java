package com.clinic.prescription;

import com.clinic.prescription.controller.PrescriptionMedicineController;
import com.clinic.prescription.entity.PrescriptionMedicine;
import com.clinic.prescription.service.PrescriptionMedicineService;
import com.clinic.prescription.util.ResponseMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionMedicineControllerTest {

    @Mock
    private PrescriptionMedicineService medicineService;

    @InjectMocks
    private PrescriptionMedicineController controller;

    @Test
    @DisplayName("POST /prescription/{prescriptionId}/medicine -> should add medicine and return saved object")
    void addMedicine_shouldReturnSavedMedicine() {
        Integer prescriptionId = 10;

        PrescriptionMedicine input = new PrescriptionMedicine();
        PrescriptionMedicine saved = new PrescriptionMedicine();

        when(medicineService.addMedicine(eq(prescriptionId), any(PrescriptionMedicine.class)))
                .thenReturn(saved);

        ResponseEntity<PrescriptionMedicine> response = controller.addMedicine(prescriptionId, input);

        assertEquals(200, response.getStatusCode().value());
        assertSame(saved, response.getBody());

        // Verify controller passes same object and same prescriptionId to service
        ArgumentCaptor<PrescriptionMedicine> captor = ArgumentCaptor.forClass(PrescriptionMedicine.class);
        verify(medicineService, times(1)).addMedicine(eq(prescriptionId), captor.capture());
        assertSame(input, captor.getValue());

        verifyNoMoreInteractions(medicineService);
    }

    @Test
    @DisplayName("GET /prescription/{prescriptionId}/medicine -> should return list of medicines")
    void getMedicines_shouldReturnList() {
        Integer prescriptionId = 22;

        List<PrescriptionMedicine> list = List.of(new PrescriptionMedicine(), new PrescriptionMedicine());
        when(medicineService.getMedicines(prescriptionId)).thenReturn(list);

        ResponseEntity<List<PrescriptionMedicine>> response = controller.getMedicines(prescriptionId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(list, response.getBody());

        verify(medicineService, times(1)).getMedicines(prescriptionId);
        verifyNoMoreInteractions(medicineService);
    }

    @Test
    @DisplayName("DELETE /prescription/medicine?id= -> should delete medicine and return message")
    void deleteMedicine_shouldDeleteAndReturnMessage() {
        Integer medicineId = 5;

        doNothing().when(medicineService).deleteMedicine(medicineId);

        ResponseEntity<ResponseMessage> response = controller.deleteMedicine(medicineId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Medicine deleted", response.getBody().getMessage());

        verify(medicineService, times(1)).deleteMedicine(medicineId);
        verifyNoMoreInteractions(medicineService);
    }
}
