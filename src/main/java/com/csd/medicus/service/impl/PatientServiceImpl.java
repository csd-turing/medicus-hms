package com.csd.medicus.service.impl;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.PatientService;
import com.csd.medicus.dto.PatientDto;
import com.csd.medicus.mapper.PatientMapper;
import com.csd.medicus.util.PhoneNormalizer;
import com.csd.medicus.util.EmailNormalizer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for patient operations.
 *
 * This class integrates phone normalization and email normalization:
 * - Phone values are normalized to E.164 before saving/updating.
 * - Email values are trimmed/lowercased and validated before saving/updating.
 *
 * Validation errors from normalizers propagate as IllegalArgumentException.
 */
@Service
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientRepository repo;

    public PatientServiceImpl(PatientRepository repo) {
        this.repo = repo;
    }

    @Override
    public Patient savePatient(Patient p) {
        // basic validation aligned with Patient model
        if (p == null) throw new IllegalArgumentException("Patient must not be null");
        if (p.getFirstName() == null || p.getFirstName().trim().length() < 2) {
            throw new IllegalArgumentException("First name required and must be at least 2 characters");
        }
        if (p.getLastName() == null || p.getLastName().trim().length() < 1) {
            throw new IllegalArgumentException("Last name required");
        }
        // normalize / trim
        p.setFirstName(p.getFirstName().trim());
        p.setLastName(p.getLastName().trim());

        // Normalize phone if present (preserve null / empty -> null)
        if (p.getPhone() != null) {
            String normalizedPhone = PhoneNormalizer.normalize(p.getPhone());
            p.setPhone(normalizedPhone);
        }

        // Normalize and validate email if present (normalize returns null for empty)
        if (p.getEmail() != null) {
            String normalizedEmail = EmailNormalizer.normalize(p.getEmail());
            p.setEmail(normalizedEmail);
        }

        return repo.save(p);
    }

    @Override
    public List<Patient> getAllPatients() {
        return repo.findAll();
    }

    @Override
    public Patient getPatientById(Long id) {
         return repo.findById(id)
        .orElseThrow(() -> new RuntimeException("Patient not found with id " + id));
    }

    @Override
    public Patient updatePatient(Long id, Patient p) {
        Patient existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Patient not found: " + id));
        if (p.getFirstName() != null && p.getFirstName().trim().length() >= 2) {
            existing.setFirstName(p.getFirstName().trim());
        }
        if (p.getLastName() != null && p.getLastName().trim().length() >= 1) {
            existing.setLastName(p.getLastName().trim());
        }
        if (p.getEmail() != null) {
            String normalizedEmail = EmailNormalizer.normalize(p.getEmail());
            existing.setEmail(normalizedEmail);
        }
        if (p.getPhone() != null) {
            // normalize before updating
            String normalized = PhoneNormalizer.normalize(p.getPhone());
            existing.setPhone(normalized);
        }
        return repo.save(existing);
    }

    @Override
    public void deletePatient(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Page<PatientDto> searchPatients(String query, Pageable pageable) {
        // Defensive: if caller passed null pageable, create a default one
        Pageable effective = pageable != null ? pageable : PageRequest.of(0, 20, Sort.unsorted());

        // Handle null query
        if (query == null) {
            return Page.empty(effective);
        }

        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            return Page.empty(effective);
        }

        // Enforce maximum page size to avoid heavy responses
        final int MAX_PAGE_SIZE = 100;
        if (effective.getPageSize() > MAX_PAGE_SIZE) {
            effective = PageRequest.of(effective.getPageNumber(), MAX_PAGE_SIZE, effective.getSort());
        }

        // Delegate to repository (JPQL handles partial + case-insensitive)
        Page<Patient> entityPage = repo.searchPatients(trimmed, effective);
        if (entityPage == null || entityPage.isEmpty()) {
            return Page.empty(effective);
        }

        // Map entities -> DTOs while keeping Page metadata
        return entityPage.map(PatientMapper::toDto);
    }
}