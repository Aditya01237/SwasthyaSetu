package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
    List<AuditLog> findByPatientId(Long patientId);
}
