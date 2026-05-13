package com.medicine.SwasthyaSetu.dto;
import lombok.Data;

@Data
public class SendOtpResponse {
    private String message;
    private String maskedEmail;
}
