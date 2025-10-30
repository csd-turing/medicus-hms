package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

@DataJpaTest
class PatientServiceImplTest {

    private PatientService patientService;
    private final PatientRepository repo;

    PatientServiceImplTest(PatientRepository repo) {
        this.repo = repo;
    }

    @BeforeEach
    void setup() {
        patientService = new PatientServiceImpl(repo);
    }

    @Test
    void testCreatePatient() {
        Patient p = new Patient(null, "John", "Doe", "john@test.com", "55555", null);
        Patient saved = patientService.savePatient(p);

        assertNotNull(saved.getId());
        assertEquals("John", saved.getFirstName());
    }

    @Test
    void testUpdatePatient() {
        Patient p = new Patient(null, "John", "Doe", "john@test.com", "55555", null);
        Patient saved = patientService.savePatient(p);

        Patient update = new Patient(null, "Johnny", "Doe", null, null, null);
        Patient updated = patientService.updatePatient(saved.getId(), update);

        assertEquals("Johnny", updated.getFirstName());
    }

    @Test
    void testGetPatient_NotFound() {
        Optional<Patient> result = patientService.getPatientById(999L);
        assertTrue(result.isEmpty());
    }
}
