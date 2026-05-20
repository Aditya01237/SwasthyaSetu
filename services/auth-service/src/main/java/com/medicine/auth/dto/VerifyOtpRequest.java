package com.medicine.auth.dto;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String uhid;
    private String otp;
}
