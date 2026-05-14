package com.example.patient_service.dto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalRecordDTO {
    private String diagnosis;
    private LocalDateTime recordDate;
    private List<MedicineDto> medicines;
}
