package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
	void testSearchPatients_paginated() {
		Patient patient = new Patient(1L, "Ram Kumar", "9876543210", "ram@example.com", "Delhi", LocalDateTime.now());
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Patient> pageFromRepo = new PageImpl<>(List.of(patient), pageable, 1);

	    when(repo.searchPatients("ram", pageable)).thenReturn(pageFromRepo);

	    Page<PatientDto> result = service.searchPatients("ram", pageable);

	    assertEquals(1, result.getTotalElements());
	    assertEquals(1, result.getContent().size());
	    assertEquals("Ram Kumar", result.getContent().get(0).getFirstName());
	    verify(repo, times(1)).searchPatients("ram", pageable);
	}

	@Test
	void testSearchByPhonePartial() {
	    Patient patient = new Patient();
	    patient.setId(1L);
	    patient.setFirstName("Ram Kumar");
	    patient.setEmail("ram@example.com");
	    patient.setPhone("9876543210");
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Patient> repoPage = new PageImpl<>(List.of(patient), pageable, 1);

	    when(repo.searchPatients(eq("9876"), any(Pageable.class))).thenReturn(repoPage);

	    Page<PatientDto> result = service.searchPatients("9876", pageable);

	    assertEquals(1L, result.getTotalElements());
	    assertEquals(1, result.getContent().size());
	    assertEquals("9876543210", result.getContent().get(0).getPhone());
	    verify(repo, times(1)).searchPatients(eq("9876"), any(Pageable.class));
	}

	@Test
	void testSearchNoResults() {
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Patient> emptyPage = Page.empty(pageable);

	    when(repo.searchPatients(eq("none"), any(Pageable.class))).thenReturn(emptyPage);

	    Page<PatientDto> result = service.searchPatients("none", pageable);

	    assertNotNull(result);
	    assertTrue(result.getContent().isEmpty());
	    assertEquals(0L, result.getTotalElements());
	    verify(repo, times(1)).searchPatients(eq("none"), any(Pageable.class));
	}

	@Test
	void testSearchMultipleResults() {
	    Patient p1 = new Patient(1L, "Ram Kumar", "9876543210", "ram@example.com", "Delhi", LocalDateTime.now());
	    Patient p2 = new Patient(2L, "Ramesh", "9876000000", "ramesh@example.com", "Delhi", LocalDateTime.now());
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<Patient> repoPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

	    when(repo.searchPatients(eq("ram"), any(Pageable.class))).thenReturn(repoPage);

	    Page<PatientDto> result = service.searchPatients("ram", pageable);

	    assertEquals(2L, result.getTotalElements());
	    assertEquals(2, result.getContent().size());
	    verify(repo, times(1)).searchPatients(eq("ram"), any(Pageable.class));
	}
}
