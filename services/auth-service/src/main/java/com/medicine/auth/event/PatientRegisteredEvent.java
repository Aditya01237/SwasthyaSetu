package com.medicine.auth.event;

public record PatientRegisteredEvent(
        Long id,
        String uhid,
        String name,
        String email,
        String phone,
        Integer age,
        String gender,
        String createdAt
) {
}
