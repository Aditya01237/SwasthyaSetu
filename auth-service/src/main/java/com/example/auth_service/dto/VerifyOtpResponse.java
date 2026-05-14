package com.example.auth_service.dto;
import com.example.auth_service.dto.PatientDto;
import lombok.Data;

@Data
public class VerifyOtpResponse {
    private PatientDto patient;
    private String message;
    private String token;
}
