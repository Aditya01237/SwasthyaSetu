package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class DoctorLoginResponse {
    private String token;
    private Long doctorId;
    private String name;
    private String email;
    private String specialization;
    private String hospitalId;
}
