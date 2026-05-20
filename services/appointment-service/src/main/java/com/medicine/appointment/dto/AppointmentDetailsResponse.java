package com.medicine.appointment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentDetailsResponse {
    private Long id;
    private String doctorName;
    private String hospitalName;
    private LocalDateTime time;
    private String qrToken;
    private Boolean isValid;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private MedicalRecordDTO medicalRecord;
}
