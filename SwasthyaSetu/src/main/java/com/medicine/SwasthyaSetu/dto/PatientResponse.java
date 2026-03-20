package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class PatientResponse {

    private String uhid;
    private String name;
    private int age;
    private String phone;
    private String gender;

}
