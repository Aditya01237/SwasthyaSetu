package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(Patient patient){
        String uhid = "UHID" + System.currentTimeMillis();
        patient.setUhid(uhid);
        patient.setCreatedAt(LocalDateTime.now());
        return patientRepository.save(patient);
    }

}
