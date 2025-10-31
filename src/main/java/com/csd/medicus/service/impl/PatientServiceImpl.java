package com.csd.medicus.service.impl;

import com.csd.medicus.model.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.PatientService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.csd.medicus.dto.PatientDto;
import com.csd.medicus.mapper.PatientMapper;


import java.util.List;
import java.util.Optional;
import java.util.stream.*;

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
        if (p.getEmail() != null) existing.setEmail(p.getEmail().trim());
        if (p.getPhone() != null) existing.setPhone(p.getPhone().trim());
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
