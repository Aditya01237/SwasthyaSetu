package com.medicine.auth.service;

import com.medicine.auth.entity.Doctor;
import com.medicine.auth.outbox.OutboxEvent;
import com.medicine.auth.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String otpRequestedRoutingKey;
    private final String doctorRegisteredRoutingKey;

    public AuthEventPublisher(OutboxEventRepository outboxRepository,
                              ObjectMapper objectMapper,
                              @Value("${app.events.exchange}") String eventsExchange,
                              @Value("${app.events.routing-keys.otp-requested}") String otpRequestedRoutingKey,
                              @Value("${app.events.routing-keys.doctor-registered}") String doctorRegisteredRoutingKey) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.otpRequestedRoutingKey = otpRequestedRoutingKey;
        this.doctorRegisteredRoutingKey = doctorRegisteredRoutingKey;
    }

    public void publishOtpRequested(String email, String otp) {
        enqueue(otpRequestedRoutingKey, new OtpRequestedEvent(email, otp), "auth.otp-requested");
    }

    public void publishDoctorRegistered(Doctor doctor) {
        DoctorRegisteredEvent event = new DoctorRegisteredEvent(
                doctor.getId(), doctor.getName(), doctor.getSpecialization(), doctor.getExperience(), doctor.getFee(),
                doctor.getEmail(), doctor.getHospital() != null ? doctor.getHospital().getId() : null
        );
        enqueue(doctorRegisteredRoutingKey, event, "doctor.registered");
    }

    private void enqueue(String routingKey, Object event, String eventName) {
        try {
            outboxRepository.save(new OutboxEvent(eventsExchange, routingKey, objectMapper.writeValueAsString(event)));
        } catch (Exception ex) {
            throw new RuntimeException("Could not persist " + eventName + " outbox event", ex);
        }
    }

    private record OtpRequestedEvent(String email, String otp) {}
    private record DoctorRegisteredEvent(Long id, String name, String specialization, int experience, int fee,
                                         String email, String hospitalId) {}
}
