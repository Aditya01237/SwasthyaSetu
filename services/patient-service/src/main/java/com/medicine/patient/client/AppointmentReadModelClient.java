package com.medicine.patient.client;

import com.medicine.patient.dto.AppointmentReadModelSnapshot;
import com.medicine.patient.dto.DoctorReadModelSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AppointmentReadModelClient {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Service-Token";

    private final RestTemplate restTemplate;
    private final String appointmentServiceBaseUrl;
    private final String internalServiceToken;

    public AppointmentReadModelClient(
            @Value("${APPOINTMENT_SERVICE_URL:http://localhost:8083}") String appointmentServiceBaseUrl,
            @Value("${app.internal.service-token}") String internalServiceToken,
            @Value("${INTERNAL_HTTP_CONNECT_TIMEOUT_MS:3000}") int connectTimeoutMs,
            @Value("${INTERNAL_HTTP_READ_TIMEOUT_MS:5000}") int readTimeoutMs) {
        this.appointmentServiceBaseUrl = appointmentServiceBaseUrl;
        this.internalServiceToken = internalServiceToken;
        this.restTemplate = createRestTemplate(connectTimeoutMs, readTimeoutMs);
    }

    public AppointmentReadModelSnapshot fetchSnapshot(Long appointmentId) {
        try {
            ResponseEntity<AppointmentReadModelSnapshot> response = restTemplate.exchange(
                    appointmentServiceBaseUrl + "/internal/appointments/" + appointmentId + "/read-model",
                    HttpMethod.GET,
                    new HttpEntity<>(internalHeaders()),
                    AppointmentReadModelSnapshot.class
            );
            if (response.getBody() == null) {
                throw new RuntimeException("Appointment not found");
            }
            return response.getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RuntimeException("Appointment not found", ex);
        }
    }

    public Optional<DoctorReadModelSnapshot> fetchDoctorSnapshot(Long doctorId) {
        try {
            ResponseEntity<DoctorReadModelSnapshot> response = restTemplate.exchange(
                    appointmentServiceBaseUrl + "/internal/doctors/" + doctorId + "/read-model",
                    HttpMethod.GET,
                    new HttpEntity<>(internalHeaders()),
                    DoctorReadModelSnapshot.class
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

    private RestTemplate createRestTemplate(int connectTimeoutMs, int readTimeoutMs) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
