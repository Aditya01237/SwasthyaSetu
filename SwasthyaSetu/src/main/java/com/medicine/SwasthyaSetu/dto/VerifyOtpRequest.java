package com.medicine.SwasthyaSetu.dto;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String uhid;
    private String otp;
}
