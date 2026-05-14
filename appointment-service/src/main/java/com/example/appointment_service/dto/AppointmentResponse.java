package com.example.appointment_service.dto;

import lombok.Data;

@Data
public class AppointmentResponse {
    private String message;
    private String qrToken;
    private String appointmentTime;
}
