package com.medicine.SwasthyaSetu.dto;
import com.medicine.SwasthyaSetu.Entity.Patient;
import lombok.Data;

@Data
public class VerifyOtpResponse {
    private Patient patient;
    private String message;
    private String token;
}
