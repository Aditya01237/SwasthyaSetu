package com.medicine.hospital.dto;

import lombok.Data;

import java.util.List;

@Data
public class HospitalDetailsResponse {
    private String id;
    private String name;
    private String city;
    private String address;
    private String phone;
    private String email;
    private List<String> imageUrls;
    private Double rating;
    private Integer totalReviews;
    private List<String> services;
    private List<String> specializations;
    private Boolean isOpen24x7;
}
