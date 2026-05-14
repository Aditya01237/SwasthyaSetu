package com.example.appointment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "hospital_id")
    private String hospitalId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    private LocalDateTime createdAt;
}
