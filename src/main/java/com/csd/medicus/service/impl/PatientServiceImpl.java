package com.csd.medicus.service.impl;

import com.csd.medicus.dto.PatientDto;
import com.csd.medicus.entity.Patient;
import com.csd.medicus.repository.PatientRepository;
import com.csd.medicus.service.PatientService;
import com.csd.medicus.mapper.PatientMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientServiceImpl(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public PatientDto createPatient(PatientDto patientDto) {
        Patient patient = PatientMapper.toEntity(patientDto);
        return PatientMapper.toDto(patientRepository.save(patient));
    }

    @Override
    public PatientDto getPatientById(Long id) {
        return patientRepository.findById(id)
                .map(PatientMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Override
    public List<PatientDto> getAllPatients() {
        return patientRepository.findAll()
                .stream().map(PatientMapper::toDto).toList();
    }

    @Override
    public PatientDto updatePatient(Long id, PatientDto patientDto) {
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        existing.setName(patientDto.getName());
        existing.setAge(patientDto.getAge());
        existing.setPhone(patientDto.getPhone());
        existing.setAddress(patientDto.getAddress());

        return PatientMapper.toDto(patientRepository.save(existing));
    }

    @Override
    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }
}
