package com.medicine.auth.dto;

import com.medicine.auth.entity.Doctor;
import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private Doctor doctor;
}
