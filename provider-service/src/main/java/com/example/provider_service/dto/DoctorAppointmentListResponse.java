package com.example.provider_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorAppointmentListResponse {
    private Long id;
    private String patientName;   // 👈 doctor sees patient
    private LocalDateTime time;
    private boolean isValid;
}