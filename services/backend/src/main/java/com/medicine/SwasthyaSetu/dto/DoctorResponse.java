package com.medicine.SwasthyaSetu.dto;
import lombok.Data;


@Data
public class DoctorResponse {

    private Long id;
    private String name;
    private String specialization;
    private Integer experience;
    private int fee;

    private String hospitalId;
    private String hospitalName;
}
