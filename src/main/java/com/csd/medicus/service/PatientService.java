package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.dto.PatientDto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {
	Patient savePatient(Patient p);

	List<Patient> getAllPatients();

	Patient getPatientById(Long id);

	Patient updatePatient(Long id, Patient p);

	void deletePatient(Long id);

	Page<PatientDto> searchPatients(String query, Pageable pageable);
}
