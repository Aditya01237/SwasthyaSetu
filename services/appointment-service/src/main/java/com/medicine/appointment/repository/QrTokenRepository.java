package com.medicine.appointment.repository;

import com.medicine.appointment.entity.QRToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QRToken, Long> {
    Optional<QRToken> findByToken(String token);
    Optional<QRToken> findByAppointmentId(Long appointmentId);
}
