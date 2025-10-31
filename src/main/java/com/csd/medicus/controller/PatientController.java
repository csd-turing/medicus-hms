package com.csd.medicus.controller;

import com.csd.medicus.dto.PatientDto;
import com.csd.medicus.mapper.PatientMapper;
import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
import com.csd.medicus.validator.PatientValidator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {
	private final PatientService service;

	public PatientController(PatientService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<PatientDto> create(@RequestBody PatientDto dto) {
		Patient p = PatientMapper.toEntity(dto);
		PatientValidator.validate(p);
		Patient saved = service.savePatient(p);
		return ResponseEntity.ok(PatientMapper.toDto(saved));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PatientDto> getById(@PathVariable Long id) {
		Patient patient = service.getPatientById(id);
		return ResponseEntity.ok(PatientMapper.toDto(patient));
	}

	@GetMapping("/search")
    public ResponseEntity<Page<com.csd.medicus.dto.PatientDto>> searchPatients(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // sanitize inputs
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100)); // apply a sensible upper cap of 100
        PageRequest pageable = PageRequest.of(safePage, safeSize);
        Page<com.csd.medicus.dto.PatientDto> result = service.searchPatients(query, pageable);
        return ResponseEntity.ok(result);
    }
}
