package com.medicine.appointment.controller;

import com.medicine.appointment.dto.DoctorReadModelResponse;
import com.medicine.appointment.entity.Doctor;
import com.medicine.appointment.repository.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/doctors")
public class InternalDoctorController {

    private final DoctorRepository doctorRepository;

    public InternalDoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping("/{id}/read-model")
    public ResponseEntity<DoctorReadModelResponse> readModel(@PathVariable("id") Long id) {
        return doctorRepository.findById(id)
                .map(d -> ResponseEntity.ok(toDto(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static DoctorReadModelResponse toDto(Doctor d) {
        String hospitalId = d.getHospital() != null ? d.getHospital().getId() : null;
        return new DoctorReadModelResponse(
                d.getId(),
                d.getEmail(),
                d.getName(),
                d.getSpecialization(),
                d.getExperience(),
                d.getFee(),
                d.getPassword(),
                hospitalId
        );
    }
}