package com.example.patient_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private Long hospitalId;
    private LocalDateTime appointmentTime;

}
