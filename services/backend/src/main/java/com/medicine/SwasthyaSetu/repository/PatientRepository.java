package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient,Long> {

    Optional<Patient> findByUhid(String uhid);
    Optional<Patient> findByPhone(String phone);

}
