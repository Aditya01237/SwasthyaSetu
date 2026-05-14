package com.example.provider_service.dto;

import com.example.provider_service.entity.Doctor;
import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private Doctor doctor;
}