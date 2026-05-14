package com.example.provider_service.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentListResponse {
    Long id;
    String doctorName;
    LocalDateTime time;
}
