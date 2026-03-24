package com.medicine.SwasthyaSetu.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientRegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 10)
    private String phone;

    @NotBlank(message = "Gender is required")
    private String gender;
}
