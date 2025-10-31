package com.csd.medicus.controller;

import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.*;
import java.util.stream.*;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	void testSearchEndpointReturnsEmptyPageWhenNoResults() throws Exception {
	    Pageable pageable = PageRequest.of(0, 10);
	    Page<com.csd.medicus.dto.PatientDto> emptyPage = Page.empty(pageable);
	    when(service.searchPatients("ram", pageable)).thenReturn(emptyPage);

	    mockMvc.perform(get("/api/v1/patients/search")
	            .param("query", "ram")
	            .param("page", "0")
	            .param("size", "10"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content").isArray())
	        .andExpect(jsonPath("$.content").isEmpty())
	        .andExpect(jsonPath("$.totalElements").value(0));

	    verify(service, times(1)).searchPatients("ram", pageable);
	}
	
	@Test
	void testSearchEndpointReturnsPageWithContent() throws Exception {
	    Pageable pageable = PageRequest.of(0, 10);
	    com.csd.medicus.dto.PatientDto dto = new com.csd.medicus.dto.PatientDto(1L, "Ram Kumar", "Kumar", "ram@example.com", "9876543210");
	    Page<com.csd.medicus.dto.PatientDto> page = new PageImpl<>(List.of(dto), pageable, 1);
	    when(service.searchPatients("ram", pageable)).thenReturn(page);

	    mockMvc.perform(get("/api/v1/patients/search")
	            .param("query", "ram")
	            .param("page", "0")
	            .param("size", "10"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content[0].firstName").value("Ram Kumar"))
	        .andExpect(jsonPath("$.totalElements").value(1));

	    verify(service, times(1)).searchPatients("ram", pageable);
	}

	@Test
	void testSearchEndpointInvokesService() throws Exception {
	    // service returns an empty page for simplicity; assert controller wiring and status
	    when(service.searchPatients(eq("9876"), any(org.springframework.data.domain.Pageable.class)))
	            .thenReturn(org.springframework.data.domain.Page.empty(org.springframework.data.domain.PageRequest.of(0, 20)));

	    mockMvc.perform(get("/api/v1/patients/search").param("query", "9876"))
	            .andExpect(status().isOk());

	    verify(service, times(1)).searchPatients(eq("9876"), any(org.springframework.data.domain.Pageable.class));
	}
	
	@Test
	void testSearchEndpointBlankQueryReturnsEmptyPage() throws Exception {
	    // service should be called but return empty page
	    Pageable expected = PageRequest.of(0, 20); // controller default size=20
	    Page<com.csd.medicus.dto.PatientDto> emptyPage = Page.empty(expected);
	    when(service.searchPatients(eq(""), any(Pageable.class))).thenReturn(emptyPage);

	    mockMvc.perform(get("/api/v1/patients/search")
	            .param("query", "") )
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content").isArray())
	        .andExpect(jsonPath("$.content").isEmpty())
	        .andExpect(jsonPath("$.totalElements").value(0));

	    verify(service, times(1)).searchPatients(eq(""), any(Pageable.class));
	}
	
	@Test
	void testSearchEndpointMissingQueryReturnsEmptyPage() throws Exception {
	    Pageable expected = PageRequest.of(0, 20);
	    Page<com.csd.medicus.dto.PatientDto> emptyPage = Page.empty(expected);
	    // controller may pass null or "" depending on implementation; accept either by stubbing any String
	    when(service.searchPatients(anyString(), any(Pageable.class))).thenReturn(emptyPage);

	    mockMvc.perform(get("/api/v1/patients/search"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content").isArray())
	        .andExpect(jsonPath("$.content").isEmpty())
	        .andExpect(jsonPath("$.totalElements").value(0));

	    verify(service, times(1)).searchPatients(anyString(), any(Pageable.class));
	}
	
	@Test
	void testSearchEndpointClampsPagingParameters() throws Exception {
	    // stub service to return an empty page for any Pageable
	    when(service.searchPatients(eq("ram"), any(Pageable.class)))
	        .thenReturn(Page.empty(PageRequest.of(0, 1)));

	    // request with page=-1 size=0 (should be clamped to page=0, size=1)
	    mockMvc.perform(get("/api/v1/patients/search")
	            .param("query", "ram")
	            .param("page", "-1")
	            .param("size", "0"))
	        .andExpect(status().isOk());

	    // Capture the pageable passed to service
	    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
	    verify(service, times(1)).searchPatients(eq("ram"), captor.capture());
	    Pageable used = captor.getValue();
	    assertEquals(0, used.getPageNumber());
	    assertTrue(used.getPageSize() >= 1);

	    // request with very large size -> should be capped to 100
	    mockMvc.perform(get("/api/v1/patients/search")
	            .param("query", "ram")
	            .param("page", "0")
	            .param("size", "1000"))
	        .andExpect(status().isOk());

	    verify(service, times(2)).searchPatients(eq("ram"), captor.capture());
	    Pageable used2 = captor.getAllValues().get(1);
	    assertTrue(used2.getPageSize() <= 100);
	}
}
