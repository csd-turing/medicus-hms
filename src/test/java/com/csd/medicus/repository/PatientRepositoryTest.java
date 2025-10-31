package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.*;
import java.util.stream.*;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

	@Test
	void testSearchByFirstNamePartial() {
		Patient p1 = new Patient();
		p1.setFirstName("Ram Kumar");
		p1.setPhone("9876543210");
		p1.setEmail("ram@example.com");
		p1.setCreatedAt(LocalDateTime.now());
		repo.saveAndFlush(p1);

		List<Patient> results = repo.searchPatients("ram");
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getPhone()).isEqualTo("9876543210");
	}

	@Test
	void testSearchByPhonePartial() {
		Patient p1 = new Patient();
		p1.setFirstName("Sita Devi");
		p1.setPhone("1234509876");
		p1.setEmail("sita@example.com");
		p1.setCreatedAt(LocalDateTime.now());
		repo.saveAndFlush(p1);

		List<Patient> results = repo.searchPatients("09876");
		assertThat(results).hasSize(1);
		assertThat(results.get(0).getFirstName()).isEqualTo("Sita Devi");
	}

	@Test
	void testCaseInsensitiveSearch() {
		Patient p1 = new Patient();
		p1.setFirstName("Ram Kumar");
		p1.setPhone("9876543210");
		p1.setEmail("ram@example.com");
		p1.setCreatedAt(LocalDateTime.now());
		repo.saveAndFlush(p1);

		// Uppercase query should still match due to LOWER(...) in JPQL
		List<Patient> results = repo.searchPatients("RAM");
		assertThat(results).hasSize(1);
	}

	@Test
	void testNoResults() {
		// no patients saved
		List<Patient> results = repo.searchPatients("doesnotexist");
		assertThat(results).isEmpty();
	}

	@Test
	void testMultipleMatches() {
		Patient p1 = new Patient();
		Patient p2 = new Patient();
		p1.setFirstName("Ram Kumar");
		p1.setPhone("9876543210");
		p1.setEmail("ram@example.com");
		p1.setCreatedAt(LocalDateTime.now());
		p2.setFirstName("Ramesh");
		p2.setPhone("9876000000");
		p2.setEmail("ramesh@example.com");
		p2.setCreatedAt(LocalDateTime.now());
		repo.saveAndFlush(p1);
		repo.saveAndFlush(p2);

		List<Patient> results = repo.searchPatients("ram");
		// both contain 'ram' in firstName
		assertThat(results.size()).isGreaterThanOrEqualTo(2);
	}

}
