package com.medicine.patient.client;

import com.medicine.patient.dto.AppointmentReadModelSnapshot;
import com.medicine.patient.dto.DoctorReadModelSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AppointmentReadModelClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String appointmentServiceBaseUrl;

    public AppointmentReadModelClient(
            @Value("${APPOINTMENT_SERVICE_URL:http://localhost:8083}") String appointmentServiceBaseUrl
    ) {
        this.appointmentServiceBaseUrl = appointmentServiceBaseUrl;
    }

    public AppointmentReadModelSnapshot fetchSnapshot(Long appointmentId) {
        try {
            AppointmentReadModelSnapshot snapshot = restTemplate.getForObject(
                    appointmentServiceBaseUrl + "/internal/appointments/" + appointmentId + "/read-model",
                    AppointmentReadModelSnapshot.class
            );
            if (snapshot == null) {
                throw new RuntimeException("Appointment not found");
            }
            return snapshot;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RuntimeException("Appointment not found", ex);
        }
    }

    public Optional<DoctorReadModelSnapshot> fetchDoctorSnapshot(Long doctorId) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    appointmentServiceBaseUrl + "/internal/doctors/" + doctorId + "/read-model",
                    DoctorReadModelSnapshot.class
            ));
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
