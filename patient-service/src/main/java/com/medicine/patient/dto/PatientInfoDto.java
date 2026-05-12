package com.medicine.patient.dto;

import lombok.Data;

@Data
public class PatientInfoDto {

    private String uhid;
    private String name;
    private String email;
    private int age;
    private String phone;
    private String gender;
}
