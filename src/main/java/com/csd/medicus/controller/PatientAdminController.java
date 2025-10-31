package com.csd.medicus.controller;

import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin controller to list (optionally include deleted), restore and purge patients.
 *
 * IMPORTANT: These endpoints are administrative and should be secured in production.
 */
@RestController
@RequestMapping("/api/v1/admin/patients")
public class PatientAdminController {

    private final PatientService service;

    public PatientAdminController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> listPatients(@RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        List<Patient> result = service.listAllPatients(includeDeleted);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Patient> restore(@PathVariable Long id) {
        Patient restored = service.restorePatient(id);
        return ResponseEntity.ok(restored);
    }

    @DeleteMapping("/{id}/purge")
    public ResponseEntity<Void> purge(@PathVariable Long id) {
        service.purgePatient(id);
        return ResponseEntity.noContent().build();
    }
}