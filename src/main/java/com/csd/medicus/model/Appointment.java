package com.csd.medicus.model;
import jakarta.persistence.*; import lombok.*; import java.time.*;
@Entity @Table(name="appointments") @Data @NoArgsConstructor @AllArgsConstructor
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private Long patientId; private Long doctorId;
    private LocalDate appointmentDate; private LocalTime appointmentTime;
    private String reason; private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime getAppointmentDateTime(){ return LocalDateTime.of(appointmentDate, appointmentTime); }
    public boolean isUpcoming(){ return getAppointmentDateTime().isAfter(LocalDateTime.now()); }
}
