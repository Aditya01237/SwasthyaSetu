package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.Entity.Patient;
import com.medicine.SwasthyaSetu.dto.PatientRegisterRequest;
import com.medicine.SwasthyaSetu.dto.PatientResponse;
import com.medicine.SwasthyaSetu.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    public PatientResponse registerPatient(PatientRegisterRequest request){

        // check duplicate phone
        patientRepository.findByPhone(request.getPhone()).ifPresent(
                p -> {
                    throw new IllegalArgumentException("Phone already registered");
                }
        );

        // create entity
        Patient patient = new Patient();
        patient.setName(request.getName());
        patient.setAge(request.getAge());
        patient.setPhone(request.getPhone());
        patient.setGender(request.getGender());

        // create uhid
        String uhid = "UHID" + System.currentTimeMillis();
        patient.setUhid(uhid);
        patient.setCreatedAt(LocalDateTime.now());

        Patient saved = patientRepository.save(patient);

        PatientResponse response = new PatientResponse();
        response.setUhid(saved.getUhid());
        response.setName(saved.getName());
        response.setAge(saved.getAge());
        response.setPhone(saved.getPhone());
        response.setGender(saved.getGender());

        return response;
    }

}
