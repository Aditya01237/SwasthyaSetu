package com.medicine.patient.service;

import com.medicine.patient.entity.Patient;
import com.medicine.patient.outbox.OutboxEvent;
import com.medicine.patient.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class PatientEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String patientRegisteredRoutingKey;

    public PatientEventPublisher(OutboxEventRepository outboxRepository,
                                 ObjectMapper objectMapper,
                                 @Value("${app.events.exchange}") String eventsExchange,
                                 @Value("${app.events.routing-keys.patient-registered}") String patientRegisteredRoutingKey) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.patientRegisteredRoutingKey = patientRegisteredRoutingKey;
    }

    public void publishPatientRegistered(Patient patient) {
        PatientRegisteredEvent event = new PatientRegisteredEvent(
                patient.getId(), patient.getUhid(), patient.getName(), patient.getEmail(), patient.getPhone(),
                patient.getAge(), patient.getGender(), patient.getCreatedAt() != null ? patient.getCreatedAt().toString() : null
        );
        try {
            outboxRepository.save(new OutboxEvent(
                    eventsExchange,
                    patientRegisteredRoutingKey,
                    objectMapper.writeValueAsString(event)
            ));
        } catch (Exception ex) {
            throw new RuntimeException("Could not persist patient.registered outbox event", ex);
        }
    }

    private record PatientRegisteredEvent(Long id, String uhid, String name, String email, String phone,
                                          Integer age, String gender, String createdAt) {}
}
