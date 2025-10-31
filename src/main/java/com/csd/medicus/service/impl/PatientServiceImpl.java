package com.csd.medicus.service.impl;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.PatientService;
import com.csd.medicus.dto.PatientDto;
import com.csd.medicus.mapper.PatientMapper;
import com.csd.medicus.util.PhoneNormalizer;
import com.csd.medicus.util.EmailNormalizer;
import com.csd.medicus.exception.DuplicateEntityException;

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
 * Responsibilities:
 * - Basic validation of required fields
 * - Normalization of phone (E.164) and email (trim/lowercase)
 * - Duplicate detection (email OR phone) before creating a new patient
 *
 * Duplicate detection occurs only on create (savePatient). Updates will not be blocked by this
 * check so that existing records can be updated (but callers should ensure they do not introduce duplicates).
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

        // Normalize and validate phone if present (preserve null / empty -> null)
        String normalizedPhone = null;
        if (p.getPhone() != null) {
            normalizedPhone = PhoneNormalizer.normalize(p.getPhone());
            p.setPhone(normalizedPhone);
        }

        // Normalize and validate email if present (normalize returns null for empty)
        String normalizedEmail = null;
        if (p.getEmail() != null) {
            normalizedEmail = EmailNormalizer.normalize(p.getEmail());
            p.setEmail(normalizedEmail);
        }

        // Duplicate detection: if either normalizedEmail or normalizedPhone is present and already exists, reject create.
        // We only run the check on create (when id is null). For updates, a different workflow may be desired.
        if (p.getId() == null) {
            boolean emailExists = normalizedEmail != null && repo.existsByEmail(normalizedEmail);
            boolean phoneExists = normalizedPhone != null && repo.existsByPhone(normalizedPhone);

            if (emailExists || phoneExists) {
                String conflictField = emailExists ? "email" : "phone";
                String conflictValue = emailExists ? normalizedEmail : normalizedPhone;
                throw new DuplicateEntityException("Patient with same " + conflictField + " already exists: " + conflictValue);
            }

            // convenience combined check as an extra guard (in case repository derivation differs)
            if ((normalizedEmail != null || normalizedPhone != null) && repo.existsByEmailOrPhone(normalizedEmail, normalizedPhone)) {
                throw new DuplicateEntityException("Patient with same email or phone already exists");
            }
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