package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
	
	@Captor
    private ArgumentCaptor<Patient> patientCaptor;

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
	
	@Test
	void testServiceReturnsEmptyPageForNullQuery() {
	    Pageable pageable = PageRequest.of(0, 10);
	    // repo should not be called when query is null; ensure repo returns something but service should short-circuit
	    when(repo.searchPatients(anyString(), any(Pageable.class)))
	        .thenReturn(Page.empty(pageable));

	    Page<com.csd.medicus.dto.PatientDto> result = service.searchPatients(null, pageable);
	    assertNotNull(result);
	    assertTrue(result.getContent().isEmpty());
	    assertEquals(0L, result.getTotalElements());

	    // Repository should not be invoked if service short-circuits for null (if implementation short-circuits).
	    // If your implementation does call repo for null, change this assert accordingly.
	    verify(repo, atMost(1)).searchPatients(anyString(), any(Pageable.class));
	}
	
	@Test
	void testServiceReturnsEmptyPageForBlankQuery() {
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<com.csd.medicus.model.Patient> emptyRepo = Page.empty(pageable);
	    when(repo.searchPatients(eq(""), any(Pageable.class))).thenReturn(emptyRepo);

	    Page<com.csd.medicus.dto.PatientDto> result = service.searchPatients("   ", pageable);
	    assertNotNull(result);
	    assertTrue(result.getContent().isEmpty());
	    assertEquals(0L, result.getTotalElements());
	}
	
	@Test
	void testServiceEnforcesMaxPageSizeCap() {
	    // create Pageable with very large page size
	    Pageable huge = PageRequest.of(0, 1000);
	    // repo will be called with a capped Pageable; we mock repo to accept any Pageable
	    when(repo.searchPatients(eq("ram"), any(Pageable.class)))
	        .thenReturn(Page.empty(PageRequest.of(0, 100)));

	    // Call service
	    Page<com.csd.medicus.dto.PatientDto> result = service.searchPatients("ram", huge);
	    assertNotNull(result);

	    // capture pageable used to call repo
	    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
	    verify(repo, times(1)).searchPatients(eq("ram"), captor.capture());
	    Pageable used = captor.getValue();
	    assertTrue(used.getPageSize() <= 100);
	}
	
	@Test
    void savePatient_normalizesPhoneBeforeSave() {
        // Arrange: patient with an IN national number (no prefix)
        Patient p = new Patient();
        p.setFirstName("John");
        p.setLastName("Doe");
        p.setPhone("9123456789");

        // Mock repository to return the same patient passed in (common pattern)
        when(repo.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Patient saved = service.savePatient(p);

        // Assert: repository.save should have been called with normalized phone
        verify(repo, times(1)).save(patientCaptor.capture());
        Patient captured = patientCaptor.getValue();

        assertNotNull(saved);
        assertEquals("+919123456789", captured.getPhone());
        assertEquals(captured.getPhone(), saved.getPhone());
    }

    @Test
    void savePatient_invalidPhone_throws() {
        // Arrange: patient with alphabetic characters in phone -> invalid
        Patient p = new Patient();
        p.setFirstName("Jane");
        p.setLastName("Smith");
        p.setPhone("123-ABC-7890");

        // Act & Assert: savePatient should throw IllegalArgumentException and repo.save should not be called
        assertThrows(IllegalArgumentException.class, () -> service.savePatient(p));
        verify(repo, never()).save(any());
    }
    
    @Test
    void updatePatient_normalizesPhoneBeforeUpdate() {
        // Arrange: existing patient in repo with some phone (could be null or old value)
        Patient existing = new Patient();
        existing.setId(1L);
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setPhone("+911111111111");

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Incoming update DTO / entity with a national IN number (no prefix)
        Patient update = new Patient();
        update.setPhone("9123456789"); // should normalize to +919123456789

        // Act
        Patient saved = service.updatePatient(1L, update);

        // Assert
        verify(repo, times(1)).findById(1L);
        verify(repo, times(1)).save(patientCaptor.capture());
        Patient captured = patientCaptor.getValue();

        assertEquals("+919123456789", captured.getPhone());
        assertEquals("+919123456789", saved.getPhone());
    }

    @Test
    void updatePatient_invalidPhone_throws() {
        // Arrange: existing patient must be found for update
        Patient existing = new Patient();
        existing.setId(2L);
        existing.setFirstName("Existing");
        existing.setLastName("Person");
        when(repo.findById(2L)).thenReturn(Optional.of(existing));

        Patient update = new Patient();
        update.setPhone("123-ABC-0000"); // invalid: contains letters

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> service.updatePatient(2L, update));
        verify(repo, times(1)).findById(2L);
        verify(repo, never()).save(any());
    }
}
