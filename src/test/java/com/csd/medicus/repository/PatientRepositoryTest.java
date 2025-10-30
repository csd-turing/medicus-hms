package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientRepositoryTest {

    private final PatientRepository repo;

    PatientRepositoryTest(PatientRepository repo) {
        this.repo = repo;
    }

    @Test
    void testSaveAndFetch() {
        Patient p = new Patient(null, "Alice", "Smith", "alice@test.com", "44444", null);
        repo.save(p);

        List<Patient> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("Alice", all.get(0).getFirstName());
    }
}
