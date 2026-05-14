package com.example.appointment_service.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentListResponse {
    Long id;
    String doctorName;
    LocalDateTime time;
}
