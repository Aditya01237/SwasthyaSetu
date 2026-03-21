package com.medicine.SwasthyaSetu.dto;
import com.medicine.SwasthyaSetu.Entity.Appointment;
import com.medicine.SwasthyaSetu.Entity.MedicalRecord;
import com.medicine.SwasthyaSetu.Entity.Patient;
import lombok.Data;
import java.util.List;

@Data
public class PatientDetailsResponse {

    private PatientInfoDto patient;
    private List<MedicalRecordDTO> medicalRecord;
    private List<AppointmentDTO> appointment;

}
