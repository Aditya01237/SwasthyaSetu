package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    List<Appointment> findByPatientId(Long patientId);

}
