package com.medicine.patient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DoctorReadModelSnapshot(
        Long id,
        String email,
        String name,
        String specialization,
        int experience,
        int fee,
        String password,
        String hospitalId
) {
}
