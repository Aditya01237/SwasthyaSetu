package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class AppointmentRequest {
    private String uhid;
    private String hospitalId;
    private Long doctorId;
    private String appointmentTime;
}
