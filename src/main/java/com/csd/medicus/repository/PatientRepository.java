package com.csd.medicus.repository;
import com.csd.medicus.model.Patient; 
import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientRepository extends JpaRepository<Patient, Long> {
  @Query("SELECT p FROM Patient p WHERE p.name LIKE %:query% OR p.mobileNumber LIKE %:query%")
List<Patient> searchPatients(@Param("query") String query);
}
