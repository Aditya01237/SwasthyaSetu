package com.medicine.patient.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Id
    private Long id;

    private String name;
    private String specialization;
    private int experience;
    private int fee;

    @Column(nullable = false, unique = true)
    private String email;

    // Legacy compatibility only. Read models no longer receive or expose credentials.
    @JsonIgnore
    @Column(nullable = true)
    private String password;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
}
