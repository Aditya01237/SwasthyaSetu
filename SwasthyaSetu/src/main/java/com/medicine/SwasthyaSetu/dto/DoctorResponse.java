package com.medicine.SwasthyaSetu.dto;

import lombok.Data;

@Data
public class DoctorResponse {
    private Long id;
    private String name;
    private String specialization;
    private int experience;
    private String hospitalName;
    private int Fee;
}
