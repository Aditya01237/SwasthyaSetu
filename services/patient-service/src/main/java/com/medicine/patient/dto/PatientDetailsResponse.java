package com.medicine.patient.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatientDetailsResponse {

    private PatientInfoDto patient;
    private List<MedicalRecordDTO> medicalRecord;
    private List<AppointmentDTO> appointment;
}
