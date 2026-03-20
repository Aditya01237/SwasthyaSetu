package com.medicine.SwasthyaSetu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientRegisterRequest {

    @NotBlank
    private String name;

    @Min(0)
    private int age;

    @NotBlank
    @Size(min = 10, max = 10)
    private String phone;

    private String gender;
}
