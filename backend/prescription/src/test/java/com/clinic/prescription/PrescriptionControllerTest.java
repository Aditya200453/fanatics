package com.clinic.prescription;

import com.clinic.prescription.controller.PrescriptionController;
import com.clinic.prescription.entity.Prescription;
import com.clinic.prescription.service.PrescriptionService;
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
class PrescriptionControllerTest {

    @Mock
    private PrescriptionService prescriptionService;

    @InjectMocks
    private PrescriptionController controller;

    @Test
    @DisplayName("GET /prescription -> should return all prescriptions")
    void getAll_shouldReturnList() {
        List<Prescription> list = List.of(new Prescription(), new Prescription());
        when(prescriptionService.getAllPrescriptions()).thenReturn(list);

        ResponseEntity<List<Prescription>> response = controller.getAll();

        assertEquals(200, response.getStatusCode().value());
        assertSame(list, response.getBody());
        verify(prescriptionService, times(1)).getAllPrescriptions();
        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("GET /prescription/{id} -> should return one prescription")
    void getOne_shouldReturnPrescription() {
        Prescription p = new Prescription();
        when(prescriptionService.getPrescription(10)).thenReturn(p);

        ResponseEntity<Prescription> response = controller.getOne(10);

        assertEquals(200, response.getStatusCode().value());
        assertSame(p, response.getBody());
        verify(prescriptionService, times(1)).getPrescription(10);
        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("POST /prescription -> should create prescription")
    void create_shouldCreatePrescription() {
        Prescription input = new Prescription();
        Prescription saved = new Prescription();

        when(prescriptionService.createPrescription(any(Prescription.class))).thenReturn(saved);

        ResponseEntity<Prescription> response = controller.create(input);

        assertEquals(200, response.getStatusCode().value());
        assertSame(saved, response.getBody());

        // Verify the exact object passed from controller to service
        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionService).createPrescription(captor.capture());
        assertSame(input, captor.getValue());

        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("PUT /prescription -> should update prescription")
    void update_shouldUpdatePrescription() {
        Prescription input = new Prescription();
        Prescription updated = new Prescription();

        when(prescriptionService.updatePrescription(any(Prescription.class))).thenReturn(updated);

        ResponseEntity<Prescription> response = controller.update(input);

        assertEquals(200, response.getStatusCode().value());
        assertSame(updated, response.getBody());

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionService).updatePrescription(captor.capture());
        assertSame(input, captor.getValue());

        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("PATCH /prescription/{id} -> should patch prescription")
    void patch_shouldPatchPrescription() {
        Integer id = 7;
        Prescription updates = new Prescription();
        Prescription patched = new Prescription();

        when(prescriptionService.patchPrescription(eq(id), any(Prescription.class))).thenReturn(patched);

        ResponseEntity<Prescription> response = controller.patch(id, updates);

        assertEquals(200, response.getStatusCode().value());
        assertSame(patched, response.getBody());

        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionService).patchPrescription(eq(id), captor.capture());
        assertSame(updates, captor.getValue());

        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("DELETE /prescription?id= -> should delete prescription and return message")
    void delete_shouldDeleteAndReturnMessage() {
        Integer id = 5;
        doNothing().when(prescriptionService).deletePrescription(id);

        ResponseEntity<ResponseMessage> response = controller.delete(id);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Prescription deleted", response.getBody().getMessage());

        verify(prescriptionService, times(1)).deletePrescription(id);
        verifyNoMoreInteractions(prescriptionService);
    }

    @Test
    @DisplayName("GET /prescription/appointment/{appointmentId} -> should return prescriptions by appointment")
    void getByAppointment_shouldReturnList() {
        Integer appointmentId = 99;
        List<Prescription> list = List.of(new Prescription());

        when(prescriptionService.getPrescriptionsByAppointment(appointmentId)).thenReturn(list);

        ResponseEntity<List<Prescription>> response = controller.getByAppointment(appointmentId);

        assertEquals(200, response.getStatusCode().value());
        assertSame(list, response.getBody());

        verify(prescriptionService, times(1)).getPrescriptionsByAppointment(appointmentId);
        verifyNoMoreInteractions(prescriptionService);
    }
}
