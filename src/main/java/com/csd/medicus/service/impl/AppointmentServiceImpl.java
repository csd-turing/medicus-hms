package com.csd.medicus.service.impl;
import com.csd.medicus.model.Appointment; import com.csd.medicus.repository.AppointmentRepository; import com.csd.medicus.service.AppointmentService;
import org.springframework.stereotype.Service; import java.time.LocalDate; import java.util.List;
@Service public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository repo;
    public AppointmentServiceImpl(AppointmentRepository repo){ this.repo = repo; }
    public Appointment createAppointment(Appointment appointment){
        if (appointment.getAppointmentDate()==null || appointment.getAppointmentTime()==null) throw new IllegalArgumentException("date/time required");
        if (!appointment.isUpcoming()) throw new IllegalArgumentException("Appointment time must be in the future.");
        LocalDate date = appointment.getAppointmentDate();
        List<Appointment> conflicts = repo.findByDoctorIdAndAppointmentDateBetween(appointment.getDoctorId(), date, date);
        if (conflicts != null && !conflicts.isEmpty()) throw new IllegalArgumentException("Conflict");
        appointment.setStatus("SCHEDULED"); return repo.save(appointment);
    }
    public List<Appointment> listAppointments(){ return repo.findAll(); }
}
