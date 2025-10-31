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

import static org.mockito.Mockito.when;
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

}
