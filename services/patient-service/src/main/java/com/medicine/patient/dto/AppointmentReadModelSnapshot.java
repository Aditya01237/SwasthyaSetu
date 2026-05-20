package com.medicine.patient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentReadModelSnapshot(
        Long appointmentId,
        String appointmentTime,
        String createdAt,
        Long sourcePatientId,
        String patientUhid,
        Long doctorId,
        String hospitalId
) {
}
