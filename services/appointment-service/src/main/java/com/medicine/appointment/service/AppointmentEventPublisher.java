package com.medicine.appointment.service;

import com.medicine.appointment.entity.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AppointmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String appointmentBookedRoutingKey;

    public AppointmentEventPublisher(RabbitTemplate rabbitTemplate,
                                     ObjectMapper objectMapper,
                                     @Value("${app.events.exchange}") String eventsExchange,
                                     @Value("${app.events.routing-keys.appointment-booked}") String appointmentBookedRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.eventsExchange = eventsExchange;
        this.appointmentBookedRoutingKey = appointmentBookedRoutingKey;
    }

    public void publishAppointmentBooked(Appointment appointment, String qrToken) {
        AppointmentBookedEvent event = new AppointmentBookedEvent(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUhid(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getDoctor().getSpecialization(),
                appointment.getHospital().getId(),
                appointment.getHospital().getName(),
                appointment.getHospital().getAddress(),
                appointment.getAppointmentTime().toString(),
                appointment.getCreatedAt() != null ? appointment.getCreatedAt().toString() : null,
                qrToken
        );

        try {
            rabbitTemplate.convertAndSend(
                    eventsExchange,
                    appointmentBookedRoutingKey,
                    objectMapper.writeValueAsString(event)
            );
        } catch (Exception ex) {
            log.warn("Could not publish appointment.booked event for appointment {}", appointment.getId(), ex);
        }
    }

    private record AppointmentBookedEvent(
            Long appointmentId,
            Long patientId,
            String patientUhid,
            String patientEmail,
            String patientName,
            Long doctorId,
            String doctorName,
            String doctorSpec,
            String hospitalId,
            String hospitalName,
            String hospitalAddress,
            String appointmentTime,
            String createdAt,
            String qrToken
    ) {
    }
}
