package com.medicine.patient.dto;

import lombok.Data;

@Data
public class QrAccessRequest {
    private Long doctorId;
    private Long appointmentId;
}
