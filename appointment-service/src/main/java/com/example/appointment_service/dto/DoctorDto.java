package com.example.appointment_service.dto;

import lombok.Data;

@Data
public class DoctorDto {
    private Long id;
    private String name;
    private String specialization;
    private int experience;
    private int fee;
    private String email;
    private String password;
}
