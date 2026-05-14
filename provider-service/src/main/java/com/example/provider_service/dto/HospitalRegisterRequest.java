package com.example.provider_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class HospitalRegisterRequest {

    // Basic Info
    private String name;
    private String city;
    private String address;

    private String phone;
    private String email;

    // Images (multiple)
    private List<String> imageUrls;

    // Rating (optional while creating)
    private Double rating;
    private Integer totalReviews;

    // Services
    private List<String> services;

    // Specializations
    private List<String> specializations;

    // Availability
    private Boolean isOpen24x7;
}