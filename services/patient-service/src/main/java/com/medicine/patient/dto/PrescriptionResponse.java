package com.medicine.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {

    private String disease;
    private List<MedicineDto> medicines;
    private String rawText;
}
