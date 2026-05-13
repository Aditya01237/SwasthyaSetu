package com.medicine.notification.dto;

public record AppointmentBookedEvent(
        Long appointmentId,
        Long patientId,
        String patientUhid,
        String patientEmail,
        String patientName,
        Long doctorId,
        String doctorName,
        String doctorSpec,
        String hospitalId,
        String hospitalName,
        String hospitalAddress,
        String appointmentTime,
        String createdAt,
        String qrToken
) {
}
