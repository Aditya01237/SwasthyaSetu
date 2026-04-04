package com.medicine.SwasthyaSetu.dto;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordDTO {

    private String diagnosis;

    private String prescription;

    private LocalDateTime recordDate;
}
