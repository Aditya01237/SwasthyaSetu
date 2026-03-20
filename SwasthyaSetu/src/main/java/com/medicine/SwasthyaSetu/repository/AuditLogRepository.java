package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {
}
