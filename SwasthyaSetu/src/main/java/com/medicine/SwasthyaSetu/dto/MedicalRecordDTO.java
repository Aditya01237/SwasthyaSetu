package com.medicine.SwasthyaSetu.dto;
import com.medicine.SwasthyaSetu.Entity.Patient;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MedicalRecordDTO {

    private String diagnosis;

    private String prescription;

    private LocalDateTime recordDate;
}
