package com.medicine.patient.repository;

import com.medicine.patient.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPatientId(Long patientId);
}
