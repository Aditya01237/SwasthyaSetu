package com.example.auth_service.dto;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String uhid;
    private String otp;
}
