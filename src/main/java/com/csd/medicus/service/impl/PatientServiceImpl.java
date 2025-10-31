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
 * Service implementation for patient operations with soft-delete support.
 *
 * Key behaviors updated for soft-delete:
 * - getAllPatients() returns only non-deleted patients.
 * - getPatientById(id) returns the patient only if not deleted.
 * - deletePatient(id) performs a soft-delete (sets isDeleted = true).
 * - searchPatients(...) already delegates to repository which filters out deleted rows.
 *
 * Duplicate detection and normalization behavior remain intact and operate on normalized values.
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

        // Duplicate detection (only on create)
        if (p.getId() == null) {
            boolean emailExists = normalizedEmail != null && repo.existsByEmailAndIsDeletedFalse(normalizedEmail);
            boolean phoneExists = normalizedPhone != null && repo.existsByPhoneAndIsDeletedFalse(normalizedPhone);

            if (emailExists || phoneExists) {
                String conflictField = emailExists ? "email" : "phone";
                String conflictValue = emailExists ? normalizedEmail : normalizedPhone;
                throw new DuplicateEntityException("Patient with same " + conflictField + " already exists: " + conflictValue);
            }

            if ((normalizedEmail != null || normalizedPhone != null) && repo.existsByEmailOrPhoneAndIsDeletedFalse(normalizedEmail, normalizedPhone)) {
                throw new DuplicateEntityException("Patient with same email or phone already exists");
            }
        }

        // Ensure new records are not accidentally marked deleted
        if (p.getId() == null) {
            p.setDeleted(false);
        }

        return repo.save(p);
    }

    @Override
    public List<Patient> getAllPatients() {
        // Use repository.findAll() but filter deleted rows — choose to call save-time repository derived method
        // Simpler approach: load all and filter, but prefer JPA query-level filtering; since no findAllNotDeleted method was added,
        // use search with empty query? For clarity and reliability, filter in memory after repo.findAll()
        List<Patient> all = repo.findAll();
        return all.stream().filter(pt -> !pt.isDeleted()).toList();
    }

    @Override
    public Patient getPatientById(Long id) {
         return repo.findByIdAndNotDeleted(id)
        .orElseThrow(() -> new RuntimeException("Patient not found with id " + id));
    }

    @Override
    public Patient updatePatient(Long id, Patient p) {
        Patient existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Patient not found: " + id));
        if (existing.isDeleted()) {
            throw new RuntimeException("Patient not found with id " + id);
        }

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
        Patient existing = repo.findById(id).orElseThrow(() -> new RuntimeException("Patient not found: " + id));
        if (existing.isDeleted()) {
            // idempotent: already deleted
            return;
        }
        existing.setDeleted(true);
        repo.save(existing);
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

        // Delegate to repository (JPQL handles partial + case-insensitive and excludes deleted rows)
        Page<Patient> entityPage = repo.searchPatients(trimmed, effective);
        if (entityPage == null || entityPage.isEmpty()) {
            return Page.empty(effective);
        }

        // Map entities -> DTOs while keeping Page metadata
        return entityPage.map(PatientMapper::toDto);
    }
}