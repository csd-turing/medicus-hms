package com.csd.medicus.service;

import com.csd.medicus.exception.DuplicateEntityException;
import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Focused tests for duplicate-detection behavior added to PatientServiceImpl.savePatient.
 *
 * These tests ensure:
 * - a create attempt with an existing email is rejected and repo.save is not called
 * - a create attempt with an existing phone is rejected and repo.save is not called
 * - a create with unique email+phone proceeds to repo.save
 *
 * The tests use the same Mockito style as the existing service tests.
 */
class PatientServiceImplDuplicateTest {

    @Mock
    private PatientRepository repo;

    @InjectMocks
    private PatientServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private Patient basePatient() {
        Patient p = new Patient();
        p.setFirstName("Alice");
        p.setLastName("Smith");
        p.setEmail("alice@example.com");
        p.setPhone("9123456789");
        return p;
    }

    @Test
    void createShouldRejectWhenEmailAlreadyExists() {
        Patient input = basePatient();
        // normalize will lowercase the email via EmailNormalizer called by service
        String normalizedEmail = "alice@example.com";

        when(repo.existsByEmail(normalizedEmail)).thenReturn(true);

        // repo.save should not be called
        assertThrows(DuplicateEntityException.class, () -> service.savePatient(input));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldRejectWhenPhoneAlreadyExists() {
        Patient input = basePatient();
        // Service will normalize phone to +91... via PhoneNormalizer
        // For the purpose of the test we stub existsByPhone to return true for the normalized value.
        String normalizedPhone = "+919123456789";

        when(repo.existsByPhone(normalizedPhone)).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> service.savePatient(input));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldProceedWhenUnique() {
        Patient input = basePatient();
        String normalizedEmail = "alice@example.com";
        String normalizedPhone = "+919123456789";

        when(repo.existsByEmail(normalizedEmail)).thenReturn(false);
        when(repo.existsByPhone(normalizedPhone)).thenReturn(false);
        when(repo.existsByEmailOrPhone(normalizedEmail, normalizedPhone)).thenReturn(false);

        Patient saved = basePatient();
        saved.setId(100L);
        saved.setEmail(normalizedEmail);
        saved.setPhone(normalizedPhone);

        when(repo.save(any())).thenReturn(saved);

        Patient result = service.savePatient(input);

        verify(repo, times(1)).save(any());
        assertEquals(100L, result.getId());
        assertEquals(normalizedEmail, result.getEmail());
        assertEquals(normalizedPhone, result.getPhone());
    }
    
    @Test
    void createShouldRejectWhenCombinedCheckFindsConflictEvenIfIndividualChecksFalse() {
        Patient input = basePatient();
        String normalizedEmail = "alice@example.com";
        String normalizedPhone = "+919123456789";

        // individual checks claim no conflict
        when(repo.existsByEmail(normalizedEmail)).thenReturn(false);
        when(repo.existsByPhone(normalizedPhone)).thenReturn(false);

        // but combined check reports a conflict (edge case)
        when(repo.existsByEmailOrPhone(normalizedEmail, normalizedPhone)).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> service.savePatient(input));
        verify(repo, never()).save(any());
    }

    @Test
    void createShouldRejectWhenOneNormalizedValueIsNullButCombinedCheckFindsConflict() {
        Patient input = basePatient();
        // Simulate empty email that normalizer will turn into null
        input.setEmail("   "); // EmailNormalizer.normalize -> null in production path
        String normalizedEmail = null;
        String normalizedPhone = "+919123456789";

        // repo.existsByEmail should not be called with null in typical Spring JPA, but we can stub defensively:
        when(repo.existsByPhone(normalizedPhone)).thenReturn(false);
        when(repo.existsByEmailOrPhone(normalizedEmail, normalizedPhone)).thenReturn(true);

        assertThrows(DuplicateEntityException.class, () -> service.savePatient(input));
        verify(repo, never()).save(any());
    }
}