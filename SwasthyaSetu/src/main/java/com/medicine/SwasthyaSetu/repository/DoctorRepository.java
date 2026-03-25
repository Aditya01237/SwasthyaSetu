package com.medicine.SwasthyaSetu.repository;
import com.medicine.SwasthyaSetu.Entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByHospitalId(String hospitalId);
}
