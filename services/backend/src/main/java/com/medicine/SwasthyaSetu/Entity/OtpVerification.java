package com.medicine.SwasthyaSetu.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "otp_verification")
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phone;

    @Column(unique = true, nullable = false)
    private String uhid;

    private String email;

    private String otp;

    private LocalDateTime expiryTime;

    private boolean verified;
}