package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientRepositoryTest {

	@Autowired
	private PatientRepository repo;

	@Test
	void testSaveAndFetch() {
		Patient p = new Patient();
		p.setFirstName("A");
		p.setLastName("B");
		p.setEmail("a@b.com");
		p.setPhone("111");

		Patient saved = repo.save(p);

		assertTrue(repo.findById(saved.getId()).isPresent());
	}

}
