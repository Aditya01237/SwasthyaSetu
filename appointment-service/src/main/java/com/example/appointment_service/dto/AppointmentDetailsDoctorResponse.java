package com.example.appointment_service.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDetailsDoctorResponse {
    private String name;
    private int age;
    private String gender;
    private LocalDateTime time;
    private Boolean isValid;
}
