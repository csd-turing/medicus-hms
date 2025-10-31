package com.csd.medicus.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;
	private LocalDateTime createdAt = LocalDateTime.now();

	/**
	 * Soft-delete flag. When true the patient is considered deleted and should be
	 * excluded from normal reads/searches. Defaults to false for new records.
	 */
	private boolean isDeleted = false;

	// convenience constructor used in tests and elsewhere
	public Patient(Long id, String firstName, String lastName, String email, String phone, LocalDateTime createdAt) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		this.createdAt = createdAt;
		this.isDeleted = false;
	}
}