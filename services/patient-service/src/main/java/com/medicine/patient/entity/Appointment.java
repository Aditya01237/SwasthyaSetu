package com.medicine.patient.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "appointments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_appointments_doctor_time",
                columnNames = {"doctor_id", "appointment_time"}
        )
)
public class Appointment {

    /** Same id as appointment-service (event-sourced read model); not DB-generated. */
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    private LocalDateTime createdAt;
}
