package com.medicine.SwasthyaSetu.Entity;

import jakarta.persistence.*;
import lombok.*;

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

    // Basic Info
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String email;

    // 🔥 Images (separate table with proper naming)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_images", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    // Rating
    private Double rating;

    private Integer totalReviews;

    // 🔥 Services (separate table)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_services", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "service")
    private List<String> services;

    // 🔥 Specializations (separate table)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_specializations", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "specialization")
    private List<String> specializations;

    // Availability
    private Boolean isOpen24x7;

    // Meta
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 🔥 Auto timestamps
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}