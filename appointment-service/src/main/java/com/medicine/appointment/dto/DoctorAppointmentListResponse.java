package com.medicine.appointment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorAppointmentListResponse {
    private Long id;
    private String patientName;
    private LocalDateTime time;
    private boolean isValid;
}
