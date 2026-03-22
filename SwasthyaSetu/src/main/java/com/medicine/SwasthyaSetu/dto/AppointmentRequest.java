package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class AppointmentRequest {
    private String uhid;
    private String hospitalId;
    private String appointmentTime;
}
