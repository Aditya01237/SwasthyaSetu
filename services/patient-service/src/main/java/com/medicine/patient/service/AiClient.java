package com.medicine.patient.service;

import com.medicine.patient.dto.PrescriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Component
public class AiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String aiServiceUrl;

    public AiClient(@Value("${ai.service.url:http://localhost:8000}") String aiServiceUrl) {
        this.aiServiceUrl = aiServiceUrl;
    }

    public PrescriptionResponse process(MultipartFile file) {
        try {
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());
            Map<String, String> request = Map.of("image", base64);
            PrescriptionResponse response = restTemplate.postForObject(
                    aiServiceUrl + "/process",
                    request,
                    PrescriptionResponse.class
            );

            if (response == null) {
                throw new RuntimeException("AI Service returned empty response");
            }

            return response;
        } catch (Exception ex) {
            throw new RuntimeException("AI Service failed", ex);
        }
    }
}
