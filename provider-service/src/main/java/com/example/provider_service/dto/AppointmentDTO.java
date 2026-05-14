package com.example.provider_service.dto;
import com.example.provider_service.entity.Hospital;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private Hospital hospital;
    private LocalDateTime appointmentTime;

}
