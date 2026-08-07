package com.medicine.auth.dto;

import lombok.Data;

@Data
public class CreateHospitalAdminRequest {
    private String email;
    private String password;
    private String hospitalId;
}
