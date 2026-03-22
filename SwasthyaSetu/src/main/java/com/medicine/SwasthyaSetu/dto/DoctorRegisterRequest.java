package com.medicine.SwasthyaSetu.dto;

import com.medicine.SwasthyaSetu.Entity.Hospital;
import lombok.Data;

@Data
public class DoctorRegisterRequest {
    private String name;
    private String specialization;
    private int experience;
    private String hospitalId;
}
