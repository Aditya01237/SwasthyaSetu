package com.example.provider_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class HospitalResponse {

    private String id;

    // Basic Info
    private String name;
    private String city;
    private String address;

    // Images (multiple)
    private List<String> imageUrls;

    // Rating Info
    private Double rating;
    private Integer totalReviews;

    // Availability
    private Boolean isOpen24x7;

    // Optional (for detailed page)
    private List<String> services;
    private List<String> specializations;
}