package com.medicine.auth.dto;

public record AdminLoginResponse(
        String token,
        Long adminId,
        String email,
        String role,
        String hospitalId
) {}
