package com.example.patient_service.repository;

import com.example.patient_service.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long> {
    List<MedicalRecord> findByPatientId(Long patientId);
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}
