package com.medicine.appointment.service;

import com.medicine.appointment.dto.PatientQrAccessRequest;
import com.medicine.appointment.dto.PatientQrAccessResponse;
import com.medicine.appointment.dto.MedicalRecordDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class PatientClinicalClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String patientServiceUrl;

    public PatientClinicalClient(@Value("${app.services.patient-url:http://localhost:8082}") String patientServiceUrl) {
        this.patientServiceUrl = patientServiceUrl;
    }

    public PatientQrAccessResponse recordQrAccess(Long patientId, Long appointmentId, Long doctorId) {
        PatientQrAccessResponse response = restTemplate.postForObject(
                patientServiceUrl + "/internal/patients/" + patientId + "/qr-access",
                new PatientQrAccessRequest(doctorId, appointmentId),
                PatientQrAccessResponse.class
        );

        if (response == null) {
            throw new RuntimeException("Patient service returned empty QR access response");
        }

        return response;
    }

    public Optional<MedicalRecordDTO> getMedicalRecordForAppointment(Long appointmentId) {
        try {
            MedicalRecordDTO response = restTemplate.getForObject(
                    patientServiceUrl + "/internal/patients/appointments/" + appointmentId + "/medical-record",
                    MedicalRecordDTO.class
            );
            return Optional.ofNullable(response);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
