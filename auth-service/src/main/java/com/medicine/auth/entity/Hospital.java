package com.medicine.auth.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    private String phone;
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_images", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    private Double rating;
    private Integer totalReviews;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_services", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "service")
    private List<String> services;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_specializations", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "specialization")
    private List<String> specializations;

    private Boolean isOpen24x7;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
