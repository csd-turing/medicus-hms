package com.csd.medicus.repository;

import com.csd.medicus.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PatientRepository extends JpaRepository<Patient, Long> {
	@Query("SELECT p FROM Patient p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :query, '%'))")
	List<Patient> searchPatients(@Param("query") String query);
}
