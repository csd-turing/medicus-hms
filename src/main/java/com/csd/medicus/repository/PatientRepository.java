package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	@Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%'))")
	Page<Patient> searchPatients(@Param("query") String query, Pageable pageable);
	
	 /**
     * Existence checks to support duplicate detection.
     * We provide both single-field existence checks and a combined check for convenience.
     */

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    /**
     * Convenience method that returns true if either email or phone matches an existing patient.
     * Implemented at the repository level by Spring Data JPA query derivation.
     */
    boolean existsByEmailOrPhone(String email, String phone);
}
