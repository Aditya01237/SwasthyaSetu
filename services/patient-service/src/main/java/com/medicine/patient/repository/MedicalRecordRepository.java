package com.medicine.patient.repository;

import com.medicine.patient.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findByPatientId(Long patientId);

    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);
}
