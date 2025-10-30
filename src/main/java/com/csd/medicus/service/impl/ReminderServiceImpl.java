package com.csd.medicus.service.impl;
import com.csd.medicus.repository.AppointmentRepository; import com.csd.medicus.service.ReminderService;
import org.springframework.stereotype.Service; import java.time.LocalDateTime; import java.util.List; import java.util.stream.Collectors;
@Service public class ReminderServiceImpl implements ReminderService {
    private final AppointmentRepository appointmentRepository;
    public ReminderServiceImpl(AppointmentRepository appointmentRepository){ this.appointmentRepository = appointmentRepository; }
    public List<String> sendRemindersNext24h(){
        LocalDateTime now = LocalDateTime.now(); LocalDateTime next = now.plusHours(24);
        return appointmentRepository.findAll().stream().filter(a -> { LocalDateTime dt = a.getAppointmentDateTime(); return dt!=null && dt.isAfter(now) && dt.isBefore(next); }).map(a -> String.format("Reminder: Patient %d with Doctor %d at %s (reason: %s)", a.getPatientId(), a.getDoctorId(), a.getAppointmentDateTime(), a.getReason())).collect(Collectors.toList());
    }
}
