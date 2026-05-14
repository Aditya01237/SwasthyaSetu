package com.medicine.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReadModelResponse {

    private Long id;
    private String email;
    private String name;
    private String specialization;
    private int experience;
    private int fee;
    private String password;
    private String hospitalId;
}
