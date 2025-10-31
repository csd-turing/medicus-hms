package com.csd.medicus.web;

import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for admin endpoints using MockMvc (standalone).
 * Uses a small TestControllerAdvice to map exceptions to HTTP responses deterministically.
 */
class PatientAdminControllerTest {

    @Mock
    private PatientService service;

    @InjectMocks
    private PatientAdminController controller;

    private MockMvc mvc;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TestControllerAdvice())
                .build();
    }

    private Patient active(Long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Active");
        p.setLastName("One");
        p.setEmail("a@example.com");
        p.setPhone("+911234567890");
        p.setCreatedAt(LocalDateTime.now());
        p.setDeleted(false);
        return p;
    }

    private Patient deleted(Long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Deleted");
        p.setLastName("Two");
        p.setEmail("d@example.com");
        p.setPhone("+910000000000");
        p.setCreatedAt(LocalDateTime.now());
        p.setDeleted(true);
        return p;
    }

    @Test
    void list_includeDeletedTrue_returnsAll() throws Exception {
        Patient a = active(1L);
        Patient b = deleted(2L);
        when(service.listAllPatients(true)).thenReturn(List.of(a, b));

        mvc.perform(get("/api/v1/admin/patients")
                .param("includeDeleted", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(service, times(1)).listAllPatients(true);
    }

    @Test
    void list_includeDeletedFalse_returnsOnlyActive() throws Exception {
        Patient a = active(3L);
        when(service.listAllPatients(false)).thenReturn(List.of(a));

        mvc.perform(get("/api/v1/admin/patients")
                .param("includeDeleted", "false")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));

        verify(service, times(1)).listAllPatients(false);
    }

    @Test
    void restore_success_returnsRestored() throws Exception {
        Patient restored = active(5L);
        restored.setDeleted(false);
        when(service.restorePatient(5L)).thenReturn(restored);

        mvc.perform(post("/api/v1/admin/patients/5/restore")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.deleted").value(false));

        verify(service, times(1)).restorePatient(5L);
    }

    @Test
    void restore_notFound_mapsToNotFound() throws Exception {
        when(service.restorePatient(99L)).thenThrow(new RuntimeException("Patient not found: 99"));

        mvc.perform(post("/api/v1/admin/patients/99/restore"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).restorePatient(99L);
    }

    @Test
    void restore_notDeleted_mapsToConflict() throws Exception {
        // Service throws IllegalStateException when the patient exists but is not deleted.
        when(service.restorePatient(10L)).thenThrow(new IllegalStateException("Patient with id 10 is not deleted"));

        mvc.perform(post("/api/v1/admin/patients/10/restore"))
                .andExpect(status().isConflict());

        verify(service, times(1)).restorePatient(10L);
    }

    @Test
    void purge_success_returnsNoContent() throws Exception {
        doNothing().when(service).purgePatient(7L);

        mvc.perform(delete("/api/v1/admin/patients/7/purge"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).purgePatient(7L);
    }

    @Test
    void purge_notFound_mapsToNotFound() throws Exception {
        doThrow(new RuntimeException("Patient not found: 33")).when(service).purgePatient(33L);

        mvc.perform(delete("/api/v1/admin/patients/33/purge"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).purgePatient(33L);
    }

    // Simple controller advice used only in tests to map RuntimeException -> 404 and IllegalStateException -> 409
    @ControllerAdvice
    static class TestControllerAdvice {

        @org.springframework.web.bind.annotation.ExceptionHandler(RuntimeException.class)
        public org.springframework.http.ResponseEntity<String> handleNotFound(RuntimeException ex) {
            return org.springframework.http.ResponseEntity.status(404).body(ex.getMessage());
        }

        @org.springframework.web.bind.annotation.ExceptionHandler(IllegalStateException.class)
        public org.springframework.http.ResponseEntity<String> handleIllegalState(IllegalStateException ex) {
            return org.springframework.http.ResponseEntity.status(409).body(ex.getMessage());
        }
    }
}