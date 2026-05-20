package com.medicine.hospital.repository;

import com.medicine.hospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(String hospitalId);
    Optional<Doctor> findByEmail(String email);
}
