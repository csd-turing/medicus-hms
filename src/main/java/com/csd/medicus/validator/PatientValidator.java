package com.csd.medicus.validator;

import com.csd.medicus.model.Patient;

public class PatientValidator {
	public static void validate(Patient p) {
		if (p == null)
			throw new IllegalArgumentException("Patient required");
		if (p.getFirstName() == null || p.getFirstName().trim().length() < 2)
			throw new IllegalArgumentException("First name too short");
	}
}
