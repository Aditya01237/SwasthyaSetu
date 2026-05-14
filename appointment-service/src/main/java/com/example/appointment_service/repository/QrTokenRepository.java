package com.example.appointment_service.repository;

import com.example.appointment_service.entity.QRToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QRToken,Long> {
    Optional<QRToken> findByToken(String token);
    Optional<QRToken> findByAppointmentId(Long appointmentId);
}
