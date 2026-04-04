package com.medicine.SwasthyaSetu.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_session")
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    private String uhid; // patient uhid OR doctor id (string)

    private String role; // PATIENT / DOCTOR

    private boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;
}