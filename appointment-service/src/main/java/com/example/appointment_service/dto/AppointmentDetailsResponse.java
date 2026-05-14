package com.example.appointment_service.dto;

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

    // ✅ ADD THESE
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    // ✅ ADD MedicalRecord
    private MedicalRecordDTO medicalRecord;
}