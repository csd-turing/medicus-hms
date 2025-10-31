package com.csd.medicus.service;

import com.csd.medicus.model.Appointment;
import java.util.List;

public interface AppointmentService {
	Appointment createAppointment(Appointment appointment);

	List<Appointment> listAppointments();
}
