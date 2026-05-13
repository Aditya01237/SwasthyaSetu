package com.medicine.SwasthyaSetu.dto;

import lombok.Data;
import java.util.List;

@Data
public class HospitalDetailsResponse {

    private String id;

    // Basic Info
    private String name;
    private String city;
    private String address;

    private String phone;
    private String email;

    // Images (gallery)
    private List<String> imageUrls;

    // Rating
    private Double rating;
    private Integer totalReviews;

    // Services & Specializations
    private List<String> services;
    private List<String> specializations;

    // Availability
    private Boolean isOpen24x7;
}