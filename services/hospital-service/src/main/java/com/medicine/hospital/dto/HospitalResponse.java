package com.medicine.hospital.dto;

import lombok.Data;

import java.util.List;

@Data
public class HospitalResponse {
    private String id;
    private String name;
    private String city;
    private String address;
    private List<String> imageUrls;
    private Double rating;
    private Integer totalReviews;
    private Boolean isOpen24x7;
    private List<String> services;
    private List<String> specializations;
}
