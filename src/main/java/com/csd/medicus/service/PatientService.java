package com.csd.medicus.service;

import com.csd.medicus.dto.PatientDto;
import java.util.List;

public interface PatientService {
    PatientDto createPatient(PatientDto patientDto);
    PatientDto getPatientById(Long id);
    List<PatientDto> getAllPatients();
    PatientDto updatePatient(Long id, PatientDto patientDto);
    void deletePatient(Long id);
}
