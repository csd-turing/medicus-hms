package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for admin operations in PatientServiceImpl.
 * Tests precisely cover success and failure paths to avoid ambiguous assertions.
 */
class PatientServiceAdminTest {

    @Mock
    private PatientRepository repo;

    @InjectMocks
    private PatientServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
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
    void listAllPatients_includesDeletedWhenFlagTrue() {
        Patient a = active(1L);
        Patient b = deleted(2L);

        when(repo.findAll()).thenReturn(List.of(a, b));
        List<Patient> res = service.listAllPatients(true);

        assertEquals(2, res.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void listAllPatients_excludesDeletedWhenFlagFalse() {
        Patient a = active(1L);
        when(repo.findAll()).thenReturn(List.of(a));
        List<Patient> res = service.listAllPatients(false);

        assertEquals(1, res.size());
        assertFalse(res.get(0).isDeleted());
        verify(repo, times(1)).findAll();
    }

    @Test
    void restorePatient_restoresDeleted() {
        Patient d = deleted(5L);
        when(repo.findById(5L)).thenReturn(Optional.of(d));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Patient restored = service.restorePatient(5L);

        assertNotNull(restored);
        assertFalse(restored.isDeleted());
        verify(repo, times(1)).save(restored);
    }

    @Test
    void restorePatient_throwsIfNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.restorePatient(99L));
        assertTrue(ex.getMessage().contains("Patient not found"));
    }

    @Test
    void restorePatient_throwsIfNotDeleted() {
        Patient a = active(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(a));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.restorePatient(10L));
        assertTrue(ex.getMessage().contains("is not deleted"));
        verify(repo, never()).save(any());
    }

    @Test
    void purgePatient_deletesWhenExists() {
        when(repo.existsById(7L)).thenReturn(true);
        doNothing().when(repo).deleteById(7L);

        service.purgePatient(7L);

        verify(repo, times(1)).deleteById(7L);
    }

    @Test
    void purgePatient_throwsWhenNotFound() {
        when(repo.existsById(33L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.purgePatient(33L));
        assertTrue(ex.getMessage().contains("Patient not found"));
        verify(repo, never()).deleteById(anyLong());
    }
}