package com.example.patient_service.repository;

import com.example.patient_service.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    List<AuditLog> findByPatientId(Long patientId);
}
