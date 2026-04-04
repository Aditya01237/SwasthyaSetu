package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByToken(String token);

    Optional<UserSession> findByUhidAndIsActiveTrue(String uhid);
}