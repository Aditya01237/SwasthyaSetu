package com.medicine.SwasthyaSetu.dto;

import com.medicine.SwasthyaSetu.Entity.MedicalRecord;
import lombok.Data;

import java.util.List;

@Data
public class QrScanResponse {
    private String status;
    private String message;
    List<String> records;
}
