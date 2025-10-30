package com.csd.medicus.service;

import com.csd.medicus.model.Patient;
import com.csd.medicus.dto.PatientDto;

import java.util.List;
import java.util.Optional;

public interface PatientService {
    Patient savePatient(Patient p);
    List<Patient> getAllPatients();
    Patient getPatientById(Long id);
    Patient updatePatient(Long id, Patient p);
    void deletePatient(Long id);
    List<PatientDto> searchPatients(String query);
}
