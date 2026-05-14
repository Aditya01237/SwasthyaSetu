package com.example.auth_service.dto;

import com.example.auth_service.dto.DoctorDto;
import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private DoctorDto doctor;
}