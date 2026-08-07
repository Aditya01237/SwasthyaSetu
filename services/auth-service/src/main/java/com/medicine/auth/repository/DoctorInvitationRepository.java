package com.medicine.auth.repository;

import com.medicine.auth.entity.DoctorInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorInvitationRepository extends JpaRepository<DoctorInvitation, Long> {
    Optional<DoctorInvitation> findByEmail(String email);
}
