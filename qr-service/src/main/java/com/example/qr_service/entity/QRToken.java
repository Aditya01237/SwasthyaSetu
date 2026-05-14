package com.example.qr_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "qr_tokens")
public class QRToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "patient_id")
    private Long patientId;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private boolean used;
}