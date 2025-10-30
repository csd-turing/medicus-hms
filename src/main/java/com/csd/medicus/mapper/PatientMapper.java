package com.csd.medicus.mapper;

import com.csd.medicus.model.Patient;
import com.csd.medicus.dto.PatientDto;

public class PatientMapper {
	public static PatientDto toDto(Patient p) {
		if (p == null)
			return null;
		return new PatientDto(p.getId(), p.getFirstName(), p.getLastName(), p.getEmail(), p.getPhone());
	}

	public static Patient toEntity(PatientDto d) {
		if (d == null)
			return null;
		Patient p = new Patient();
		p.setId(d.getId());
		p.setFirstName(d.getFirstName());
		p.setLastName(d.getLastName());
		p.setEmail(d.getEmail());
		p.setPhone(d.getPhone());
		return p;
	}
}
