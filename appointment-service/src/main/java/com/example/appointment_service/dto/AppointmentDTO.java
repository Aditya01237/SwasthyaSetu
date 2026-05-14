package com.example.appointment_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private String hospitalId;
    private LocalDateTime appointmentTime;

}
