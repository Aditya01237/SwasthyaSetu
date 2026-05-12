package com.medicine.SwasthyaSetu.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionResponse {
    private String disease;
    private List<MedicineDto> medicines;
    private String rawText;
}
