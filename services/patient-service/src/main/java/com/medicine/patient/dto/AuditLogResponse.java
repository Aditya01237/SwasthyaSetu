package com.medicine.patient.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {

    private String doctorName;
    private String action;
    private LocalDateTime timestamp;
}
