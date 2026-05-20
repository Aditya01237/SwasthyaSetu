package com.medicine.auth.dto;

import lombok.Data;

@Data
public class DoctorRegisterRequest {
    private String name;
    private String specialization;
    private int experience;
    private int fee;
    private String email;
    private String password;
    private String hospitalId;
}
