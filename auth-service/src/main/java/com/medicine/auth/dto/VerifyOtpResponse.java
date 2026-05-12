package com.medicine.auth.dto;

import com.medicine.auth.entity.Patient;
import lombok.Data;

@Data
public class VerifyOtpResponse {
    private Patient patient;
    private String message;
    private String token;
}
