package com.example.auth_service.dto;
import lombok.Data;

@Data
public class SendOtpResponse {
    private String message;
    private String maskedEmail;
}
