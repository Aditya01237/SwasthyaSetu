package com.medicine.SwasthyaSetu.repository;

import com.medicine.SwasthyaSetu.Entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospitalRepository extends JpaRepository<Hospital,String> {
    List<Hospital> findByCityContainingIgnoreCase(String city);
    List<Hospital> findByNameContainingIgnoreCase(String name);
    List<Hospital> findByCityContainingIgnoreCaseAndNameContainingIgnoreCase(String city, String name);
}
