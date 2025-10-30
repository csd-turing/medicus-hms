package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.csd.medicus.dto.PatientDto;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceImplTest {

    @Mock
    private PatientRepository repo;

    @InjectMocks
    private PatientServiceImpl service;

    @BeforeEach
void init() {
    MockitoAnnotations.openMocks(this);
}

private Patient createPatient(Long id) {
    Patient p = new Patient();
    p.setId(id);
    p.setFirstName("John");
    p.setLastName("Doe");
    p.setEmail("john@example.com");
    p.setPhone("1234567890");
    return p;
}

@Test
void testCreatePatient() {
    Patient input = createPatient(null);
    Patient saved = createPatient(1L);

    when(repo.save(any())).thenReturn(saved);

    Patient result = service.savePatient(input);
    assertEquals(1L, result.getId());
}

@Test
void testGetPatient_NotFound() {
    when(repo.findById(1L)).thenReturn(Optional.empty());
    assertThrows(RuntimeException.class, () -> service.getPatientById(1L));
}

@Test
void testUpdatePatient() {
    Patient existing = createPatient(1L);
    Patient updated = createPatient(1L);
    updated.setLastName("Smith");

    when(repo.findById(1L)).thenReturn(Optional.of(existing));
    when(repo.save(any())).thenReturn(updated);

    Patient result = service.updatePatient(1L, updated);
    assertEquals("Smith", result.getLastName());
}

@Test
void testSearchPatients() {
Patient patient = new Patient(
        1L,
        "Ram Kumar",
        "9876543210",
        "ram@example.com",
        "Delhi",
        LocalDateTime.now()
);
    when(repo.searchPatients("ram"))
            .thenReturn(List.of(patient));

    List<PatientDto> result = service.searchPatients("ram");

    assertEquals(1, result.size());
    verify(repo, times(1)).searchPatients("ram");
}

}
