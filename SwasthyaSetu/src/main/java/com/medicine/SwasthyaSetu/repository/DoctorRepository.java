package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
