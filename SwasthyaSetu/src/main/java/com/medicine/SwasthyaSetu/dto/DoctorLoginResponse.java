package com.medicine.SwasthyaSetu.dto;

import com.medicine.SwasthyaSetu.Entity.Doctor;
import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private Doctor doctor;
}