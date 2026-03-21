package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class AppointmentRequest {
    private String uhid;
    private Long hospitalId;
    private String appointmentTime;
}
