package com.example.appointment_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PatientDto {
    private Long id;
    private String uhid;
    private String name;
    private String email;
    private int age;
    private String phone;
    private String gender;
    private LocalDateTime createdAt;
}
