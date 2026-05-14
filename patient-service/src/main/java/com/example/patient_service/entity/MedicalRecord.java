package com.example.patient_service.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_records")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "appointment_id")
    private Long appointmentId;

    private String diagnosis;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<Medicine> medicines;

    private LocalDateTime recordDate;
}