package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

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
	void testSearchByFirstNamePartial_paginated() {
	    Patient p1 = new Patient();
	    p1.setFirstName("Ram Kumar");
	    p1.setPhone("9876543210");
	    p1.setEmail("ram@example.com");
	    p1.setCreatedAt(LocalDateTime.now());
	    repo.saveAndFlush(p1);

	    PageRequest pr = PageRequest.of(0, 10);
	    Page<Patient> results = repo.searchPatients("ram", pr);

	    assertThat(results).isNotNull();
	    assertThat(results.getContent()).hasSize(1);
	    assertThat(results.getContent().get(0).getPhone()).isEqualTo("9876543210");
	    assertThat(results.getTotalElements()).isEqualTo(1L);
	}

	@Test
	void testSearchByPhonePartial_paginated() {
	    Patient p1 = new Patient();
	    p1.setFirstName("Sita Devi");
	    p1.setPhone("1234509876");
	    p1.setEmail("sita@example.com");
	    p1.setCreatedAt(LocalDateTime.now());
	    repo.saveAndFlush(p1);

	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Patient> results = repo.searchPatients("09876", pageable);

	    assertThat(results).isNotNull();
	    assertThat(results.getContent()).hasSize(1);
	    assertThat(results.getContent().get(0).getFirstName()).isEqualTo("Sita Devi");
	    assertThat(results.getTotalElements()).isEqualTo(1L);
	}

	@Test
	void testCaseInsensitiveSearch_paginated() {
	    Patient p1 = new Patient();
	    p1.setFirstName("Ram Kumar");
	    p1.setPhone("9876543210");
	    p1.setEmail("ram@example.com");
	    p1.setCreatedAt(LocalDateTime.now());
	    repo.saveAndFlush(p1);

	    Pageable pageable = PageRequest.of(0, 10);
	    // Uppercase query should still match due to LOWER(...) in JPQL
	    Page<Patient> results = repo.searchPatients("RAM", pageable);

	    assertThat(results).isNotNull();
	    assertThat(results.getContent()).hasSize(1);
	    assertThat(results.getTotalElements()).isEqualTo(1L);
	}

	@Test
	void testNoResults_paginated() {
	    // no patients saved for this query
	    PageRequest pageable = PageRequest.of(0, 10);
	    Page<Patient> results = repo.searchPatients("doesnotexist", pageable);

	    assertThat(results).isNotNull();
	    assertThat(results.getContent()).isEmpty();
	    assertThat(results.getTotalElements()).isEqualTo(0L);
	}

	@Test
	void testMultipleMatches_paginated() {
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

	    PageRequest pageable = PageRequest.of(0, 10);
	    Page<Patient> results = repo.searchPatients("ram", pageable);

	    assertThat(results).isNotNull();
	    // ensure both matching entries are returned (totalElements >= 2)
	    assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(2L);
	    assertThat(results.getContent()).isNotEmpty();
	}
	
	@Test
	void testSearchByFirstNamePartial_paginated_excludesDeleted() {
	    Patient p1 = new Patient();
	    p1.setFirstName("Ram Kumar");
	    p1.setPhone("9876543210");
	    p1.setEmail("ram@example.com");
	    p1.setCreatedAt(LocalDateTime.now());
	    p1.setDeleted(false);
	    repo.saveAndFlush(p1);

	    Patient deleted = new Patient();
	    deleted.setFirstName("Ram Deleted");
	    deleted.setPhone("0000000000");
	    deleted.setEmail("ramdel@example.com");
	    deleted.setCreatedAt(LocalDateTime.now());
	    deleted.setDeleted(true);
	    repo.saveAndFlush(deleted);

	    PageRequest pr = PageRequest.of(0, 10);
	    Page<Patient> results = repo.searchPatients("ram", pr);

	    assertThat(results).isNotNull();
	    assertThat(results.getContent()).hasSize(1);
	    assertThat(results.getContent().get(0).getPhone()).isEqualTo("9876543210");
	    assertThat(results.getTotalElements()).isEqualTo(1L);
	}
}
