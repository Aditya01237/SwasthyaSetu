package com.medicine.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot for patient-service read model (internal API).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentReadModelResponse {

    private Long appointmentId;
    private String appointmentTime;
    private String createdAt;
    private Long sourcePatientId;
    private String patientUhid;
    private Long doctorId;
    private String hospitalId;
}
