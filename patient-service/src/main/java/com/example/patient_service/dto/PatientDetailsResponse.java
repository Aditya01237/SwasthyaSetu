package com.example.patient_service.dto;

import com.example.patient_service.entity.MedicalRecord;
import com.example.patient_service.entity.Patient;
import lombok.Data;
import java.util.List;

@Data
public class PatientDetailsResponse {

    private PatientInfoDto patient;
    private List<MedicalRecordDTO> medicalRecord;
    private List<AppointmentDTO> appointment;

}
