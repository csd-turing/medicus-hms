package com.csd.medicus.repository;
import com.csd.medicus.model.Appointment; import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate; import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorIdAndAppointmentDateBetween(Long doctorId, LocalDate start, LocalDate end);
    List<Appointment> findByAppointmentDateBetween(LocalDate start, LocalDate end);
}
