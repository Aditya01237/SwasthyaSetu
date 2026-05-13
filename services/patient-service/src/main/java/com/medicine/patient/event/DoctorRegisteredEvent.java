package com.medicine.patient.event;

public record DoctorRegisteredEvent(
        Long id,
        String name,
        String specialization,
        int experience,
        int fee,
        String email,
        String password,
        String hospitalId
) {
}
