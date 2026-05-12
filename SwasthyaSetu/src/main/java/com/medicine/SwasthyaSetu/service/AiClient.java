package com.medicine.SwasthyaSetu.service;

import com.medicine.SwasthyaSetu.dto.PrescriptionResponse;
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
            // convert image → base64
            String base64 = Base64.getEncoder().encodeToString(file.getBytes());

            String url = aiServiceUrl + "/process";

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
