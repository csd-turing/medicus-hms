package com.csd.medicus.controller;
import com.csd.medicus.dto.PatientDto; import com.csd.medicus.mapper.PatientMapper; import com.csd.medicus.model.Patient; import com.csd.medicus.service.PatientService; import com.csd.medicus.validator.PatientValidator; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/patients") public class PatientController {
    private final PatientService service;
    public PatientController(PatientService service){ this.service=service; }
    @PostMapping public ResponseEntity<PatientDto> create(@RequestBody PatientDto dto){ Patient p=PatientMapper.toEntity(dto); PatientValidator.validate(p); Patient saved=service.savePatient(p); return ResponseEntity.ok(PatientMapper.toDto(saved)); }
@GetMapping("/{id}")
public ResponseEntity<PatientDto> getById(@PathVariable Long id) {
    Patient patient = service.getPatientById(id);
    return ResponseEntity.ok(PatientMapper.toDto(patient));
}
}
