package com.medicine.patient.dto;

import lombok.Data;

import java.util.List;

@Data
public class QrAccessResponse {
    private String status;
    private String message;
    private List<MedicalRecordDTO> records;
}
