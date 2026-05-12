package com.medicine.patient.dto;

import com.medicine.patient.entity.Hospital;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDTO {

    private Hospital hospital;
    private LocalDateTime appointmentTime;
}
