package com.example.patient_service.service;

import com.example.patient_service.dto.PrescriptionResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@Component
public class AiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public PrescriptionResponse process(MultipartFile file) {

        try {
            // convert image → base64
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            String url = "http://localhost:8000/process";

            Map<String, String> request = Map.of("image", base64);

            return restTemplate.postForObject(
                    url,
                    request,
                    PrescriptionResponse.class
            );

        } catch (Exception e) {
            throw new RuntimeException("AI Service failed", e);
        }
    }
}