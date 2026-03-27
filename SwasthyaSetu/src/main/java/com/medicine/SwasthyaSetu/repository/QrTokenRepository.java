package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.QRToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QrTokenRepository extends JpaRepository<QRToken,Long> {
    Optional<QRToken> findByToken(String token);
    QRToken findByAppointmentId(Long id);
}
