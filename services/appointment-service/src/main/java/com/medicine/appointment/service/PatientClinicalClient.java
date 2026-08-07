package com.medicine.appointment.service;

import com.medicine.appointment.dto.MedicalRecordDTO;
import com.medicine.appointment.dto.PatientQrAccessRequest;
import com.medicine.appointment.dto.PatientQrAccessResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class PatientClinicalClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Service-Token";

    private final RestTemplate restTemplate = new RestTemplate();
    private final String patientServiceUrl;
    private final String internalServiceToken;

    public PatientClinicalClient(
            @Value("${app.services.patient-url:http://localhost:8082}") String patientServiceUrl,
            @Value("${app.internal.service-token}") String internalServiceToken) {
        this.patientServiceUrl = patientServiceUrl;
        this.internalServiceToken = internalServiceToken;
    }

    public PatientQrAccessResponse recordQrAccess(Long appointmentId, Long doctorId) {
        HttpHeaders headers = internalHeaders();
        HttpEntity<PatientQrAccessRequest> request = new HttpEntity<>(
                new PatientQrAccessRequest(doctorId, appointmentId), headers);

        ResponseEntity<PatientQrAccessResponse> response = restTemplate.exchange(
                patientServiceUrl + "/internal/appointments/" + appointmentId + "/qr-access",
                HttpMethod.POST,
                request,
                PatientQrAccessResponse.class
        );

        if (response.getBody() == null) {
            throw new RuntimeException("Patient service returned empty QR access response");
        }
        return response.getBody();
    }

    public Optional<MedicalRecordDTO> getMedicalRecordForAppointment(Long appointmentId) {
        try {
            ResponseEntity<MedicalRecordDTO> response = restTemplate.exchange(
                    patientServiceUrl + "/internal/patients/appointments/" + appointmentId + "/medical-record",
                    HttpMethod.GET,
                    new HttpEntity<>(internalHeaders()),
                    MedicalRecordDTO.class
            );
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }

    private HttpHeaders internalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(INTERNAL_TOKEN_HEADER, internalServiceToken);
        return headers;
    }
}
