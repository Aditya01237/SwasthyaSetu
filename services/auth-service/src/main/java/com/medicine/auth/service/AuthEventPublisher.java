package com.medicine.auth.service;

import com.medicine.auth.entity.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AuthEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AuthEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String otpRequestedRoutingKey;
    private final String doctorRegisteredRoutingKey;

    public AuthEventPublisher(RabbitTemplate rabbitTemplate,
                              ObjectMapper objectMapper,
                              @Value("${app.events.exchange}") String eventsExchange,
                              @Value("${app.events.routing-keys.otp-requested}") String otpRequestedRoutingKey,
                              @Value("${app.events.routing-keys.doctor-registered}") String doctorRegisteredRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.otpRequestedRoutingKey = otpRequestedRoutingKey;
        this.doctorRegisteredRoutingKey = doctorRegisteredRoutingKey;
    }

    public void publishOtpRequested(String email, String otp) {
        OtpRequestedEvent event = new OtpRequestedEvent(email, otp);

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    otpRequestedRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.error("Could not publish auth.otp-requested event for {}", email, ex);
            throw new RuntimeException("Could not queue OTP email", ex);
        }
    }

    public void publishDoctorRegistered(Doctor doctor) {
        DoctorRegisteredEvent event = new DoctorRegisteredEvent(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialization(),
                doctor.getExperience(),
                doctor.getFee(),
                doctor.getEmail(),
                doctor.getPassword(),
                doctor.getHospital() != null ? doctor.getHospital().getId() : null
        );

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    doctorRegisteredRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.warn("Could not publish doctor.registered event for {}", doctor.getEmail(), ex);
        }
    }

    private record OtpRequestedEvent(String email, String otp) {
    }

    private record DoctorRegisteredEvent(
            Long id,
            String name,
            String specialization,
            int experience,
            int fee,
            String email,
            String password,
            String hospitalId
    ) {
    }
}
