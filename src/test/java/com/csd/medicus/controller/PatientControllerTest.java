package com.csd.medicus.controller;

import com.csd.medicus.model.Patient;
import com.csd.medicus.service.PatientService;
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
    MockMvc mvc;

    @MockBean
    PatientService service;

    @Test
    void testGetPatientById() throws Exception {
        Patient p = new Patient(1L, "Sam", "Green", "sam@test.com", "11111", null);
        when(service.getPatientById(1L)).thenReturn(Optional.of(p));

        mvc.perform(get("/api/patients/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.firstName").value("Sam"));
    }
}
