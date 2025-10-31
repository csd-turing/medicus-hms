package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	@Query("SELECT p FROM Patient p WHERE p.isDeleted = false AND (LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%')))")
	Page<Patient> searchPatients(@Param("query") String query, Pageable pageable);

	// Existence checks used by earlier duplicate-detection feature (they should
	// consider only non-deleted)
	boolean existsByEmailAndIsDeletedFalse(String email);

	boolean existsByPhoneAndIsDeletedFalse(String phone);

	boolean existsByEmailOrPhoneAndIsDeletedFalse(String email, String phone);

	// Override findById semantics are not possible via method signature; callers
	// must respect soft-delete.
	// Provide a helper finder that excludes deleted rows:
	@Query("SELECT p FROM Patient p WHERE p.id = :id AND p.isDeleted = false")
	java.util.Optional<Patient> findByIdAndNotDeleted(@Param("id") Long id);
}
