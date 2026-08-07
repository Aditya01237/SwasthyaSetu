package com.medicine.auth.dto;

import lombok.Data;

@Data
public class DoctorRegisterRequest {
    private String email;
    private String password;
}
