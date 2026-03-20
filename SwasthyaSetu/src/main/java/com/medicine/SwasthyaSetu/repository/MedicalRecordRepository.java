package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long> {
    List<MedicalRecord> findByPatientId(Long patientId);
}
