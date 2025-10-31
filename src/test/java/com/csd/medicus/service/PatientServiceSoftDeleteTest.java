package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for soft-delete behavior in PatientServiceImpl.
 *
 * These tests assert:
 * - deletePatient marks isDeleted = true and saves the entity (idempotent)
 * - getAllPatients excludes soft-deleted patients
 * - getPatientById does not return a soft-deleted patient (throws)
 */
class PatientServiceSoftDeleteTest {

    @Mock
    private PatientRepository repo;

    @InjectMocks
    private PatientServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private Patient activePatient(Long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Active");
        p.setLastName("Person");
        p.setEmail("active@example.com");
        p.setPhone("+911234567890");
        p.setCreatedAt(LocalDateTime.now());
        p.setDeleted(false);
        return p;
    }

    private Patient deletedPatient(Long id) {
        Patient p = new Patient();
        p.setId(id);
        p.setFirstName("Deleted");
        p.setLastName("Person");
        p.setEmail("deleted@example.com");
        p.setPhone("+911000000000");
        p.setCreatedAt(LocalDateTime.now());
        p.setDeleted(true);
        return p;
    }

    @Test
    void deletePatientMarksDeletedAndSaves() {
        Patient existing = activePatient(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deletePatient(10L);

        // verify repository save was called and the saved entity was marked deleted
        verify(repo, times(1)).save(existing);
        assertTrue(existing.isDeleted());
    }

    @Test
    void deletePatientIdempotentWhenAlreadyDeleted() {
        Patient existing = deletedPatient(11L);
        when(repo.findById(11L)).thenReturn(Optional.of(existing));

        // should not throw and should not call save (since already deleted)
        service.deletePatient(11L);

        verify(repo, never()).save(any());
        assertTrue(existing.isDeleted());
    }

    @Test
    void getAllPatientsExcludesDeleted() {
        Patient a = activePatient(1L);
        Patient b = deletedPatient(2L);
        when(repo.findAll()).thenReturn(List.of(a, b));

        List<Patient> result = service.getAllPatients();

        assertEquals(1, result.size());
        assertEquals(a.getId(), result.get(0).getId());
    }

    @Test
    void getPatientByIdThrowsWhenDeleted() {
        Patient deleted = deletedPatient(20L);
        when(repo.findById(20L)).thenReturn(Optional.of(deleted));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.getPatientById(20L));
        assertTrue(ex.getMessage().contains("Patient not found"));
    }
}