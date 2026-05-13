package com.medicine.auth.repository;

import com.medicine.auth.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByUhid(String uhid);
    Optional<OtpVerification> findByEmail(String email);
}
