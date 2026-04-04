package com.medicine.SwasthyaSetu.dto;
import lombok.Data;
import java.util.List;

@Data
public class QrScanResponse {
    private String status;
    private String message;
    private List<MedicalRecordDTO> records;
}
