package com.example.qr_service.service;

import com.example.qr_service.entity.*;
import com.example.qr_service.dto.*;
import com.example.qr_service.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.example.qr_service.config.RabbitMQConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QrService {

    private final QrTokenRepository qrTokenRepository;
    private final RestTemplate restTemplate;
    private final RabbitTemplate rabbitTemplate;

    public QrService(QrTokenRepository qrTokenRepository, RestTemplate restTemplate, RabbitTemplate rabbitTemplate){
        this.qrTokenRepository = qrTokenRepository;
        this.restTemplate = restTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    public QrScanResponse scan(QrScanRequest request){

        // Find token
        QRToken qr = qrTokenRepository.findByToken(request.getToken()).orElseThrow(
                () -> new RuntimeException("Invalid QR"));

        // Check time validity
        LocalDateTime currTime = LocalDateTime.now();
        if(currTime.isBefore(qr.getValidFrom()) || currTime.isAfter(qr.getValidTo())){
            throw new RuntimeException("QR Code Expired");
        }

        // Check already used
        if(qr.isUsed()){
            throw new RuntimeException("QR already used");
        }

        // Fetch medical records & save audit log via patient-service
        List<MedicalRecordDTO> data;
        try {
            ResponseEntity<List<MedicalRecordDTO>> response = restTemplate.exchange(
                    "http://patient-service/api/patient/internal/" + qr.getPatientId() + "/scan-access?doctorId=" + request.getDoctorId(),
                    HttpMethod.POST,
                    null,
                    new ParameterizedTypeReference<List<MedicalRecordDTO>>() {}
            );
            data = response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve patient records", e);
        }

        // Mark QR as used
        qr.setUsed(true);
        qrTokenRepository.save(qr);

        // Publish Event to RabbitMQ
        QrScannedEvent event = new QrScannedEvent(
                qr.getPatientId(),
                request.getDoctorId(),
                "QR_SCAN",
                LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.QR_AUDIT_QUEUE, event);

        // Response
        QrScanResponse response = new QrScanResponse();
        response.setStatus("SUCCESS");
        response.setMessage("Access Granted");
        response.setRecords(data);

        return response;
    }
}
