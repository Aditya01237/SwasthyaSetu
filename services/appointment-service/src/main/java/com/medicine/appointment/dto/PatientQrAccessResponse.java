package com.medicine.appointment.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatientQrAccessResponse {
    private String status;
    private String message;
    private List<MedicalRecordDTO> records;
}
