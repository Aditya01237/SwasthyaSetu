package com.example.patient_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrScannedEvent {
    private Long patientId;
    private Long doctorId;
    private String action;
    private LocalDateTime timestamp;
}
