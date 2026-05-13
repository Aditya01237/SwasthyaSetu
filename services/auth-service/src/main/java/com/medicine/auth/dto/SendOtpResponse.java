package com.medicine.auth.dto;

import lombok.Data;

@Data
public class SendOtpResponse {
    private String message;
    private String maskedEmail;
}
