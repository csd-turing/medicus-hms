package com.csd.medicus.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
}
