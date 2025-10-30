package com.csd.medicus.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="patients") @Data @NoArgsConstructor @AllArgsConstructor
public class Patient {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private String firstName; private String lastName; private String email; private String phone;
    private LocalDateTime createdAt = LocalDateTime.now();
}
