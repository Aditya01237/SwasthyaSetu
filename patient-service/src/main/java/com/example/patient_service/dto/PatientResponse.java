package com.example.patient_service.dto;

import lombok.Data;

@Data
public class PatientResponse {

    private String uhid;
    private String name;
    private String email;
    private int age;
    private String phone;
    private String gender;

}
