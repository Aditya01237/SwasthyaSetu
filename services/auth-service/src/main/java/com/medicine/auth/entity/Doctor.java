package com.medicine.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors")
public class Doctor {

    // Doctor profile identity is owned by hospital-service. Auth stores the
    // credential row with the same ID so JWT subject matches appointment data.
    @Id
    private Long id;

    private String name;
    private String specialization;
    private int experience;
    private int fee;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Hospital hospital;
}
