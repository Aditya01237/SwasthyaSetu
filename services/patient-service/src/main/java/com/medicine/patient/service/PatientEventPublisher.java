package com.medicine.patient.service;

import com.medicine.patient.entity.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class PatientEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PatientEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String patientRegisteredRoutingKey;

    public PatientEventPublisher(RabbitTemplate rabbitTemplate,
                                 ObjectMapper objectMapper,
                                 @Value("${app.events.exchange}") String eventsExchange,
                                 @Value("${app.events.routing-keys.patient-registered}") String patientRegisteredRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.patientRegisteredRoutingKey = patientRegisteredRoutingKey;
    }

    public void publishPatientRegistered(Patient patient) {
        PatientRegisteredEvent event = new PatientRegisteredEvent(
                patient.getId(),
                patient.getUhid(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getAge(),
                patient.getGender(),
                patient.getCreatedAt() != null ? patient.getCreatedAt().toString() : null
        );

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    patientRegisteredRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.warn("Could not publish patient.registered event for {}", patient.getUhid(), ex);
        }
    }

    private record PatientRegisteredEvent(
            Long id,
            String uhid,
            String name,
            String email,
            String phone,
            Integer age,
            String gender,
            String createdAt
    ) {
    }
}
