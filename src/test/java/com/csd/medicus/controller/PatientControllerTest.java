package com.csd.medicus.controller;

import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.*;
import java.util.stream.*;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
class PatientControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private PatientService service;

	@Autowired
	private ObjectMapper mapper;

	@Test
	void testGetPatientById() throws Exception {
		Patient p = new Patient();
		p.setId(1L);
		p.setFirstName("John");
		p.setLastName("Doe");
		p.setEmail("john@example.com");
		p.setPhone("1234567890");

		when(service.getPatientById(1L)).thenReturn(p);

		mockMvc.perform(get("/api/v1/patients/1")).andExpect(status().isOk());
	}

	@Test
	void testSearchEndpointReturnsEmptyArrayWhenNoResults() throws Exception {
		when(service.searchPatients("ram")).thenReturn(Collections.emptyList());

		mockMvc.perform(get("/api/v1/patients/search").param("query", "ram")).andExpect(status().isOk())
				.andExpect(content().json("[]"));

		verify(service, times(1)).searchPatients("ram");
	}

	@Test
	void testSearchEndpointInvokesService() throws Exception {
		// service returns an empty list for simplicity; we only assert controller
		// wiring and status
		when(service.searchPatients("9876")).thenReturn(List.of());

		mockMvc.perform(get("/api/v1/patients/search").param("query", "9876")).andExpect(status().isOk());

		verify(service, times(1)).searchPatients("9876");
	}
}
