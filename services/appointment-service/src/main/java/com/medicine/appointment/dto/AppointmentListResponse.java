package com.medicine.appointment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentListResponse {
    private Long id;
    private String doctorName;
    private LocalDateTime time;
}
