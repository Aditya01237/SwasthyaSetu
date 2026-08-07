package com.medicine.appointment.service;

import com.medicine.appointment.entity.Appointment;
import com.medicine.appointment.outbox.OutboxEvent;
import com.medicine.appointment.outbox.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AppointmentEventPublisher {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final String eventsExchange;
    private final String appointmentBookedRoutingKey;

    public AppointmentEventPublisher(OutboxEventRepository outboxRepository,
                                     ObjectMapper objectMapper,
                                     @Value("${app.events.exchange}") String eventsExchange,
                                     @Value("${app.events.routing-keys.appointment-booked}") String appointmentBookedRoutingKey) {
        this.outboxRepository = outboxRepository;
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
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(new OutboxEvent(eventsExchange, appointmentBookedRoutingKey, payload));
        } catch (Exception ex) {
            throw new RuntimeException("Could not persist appointment.booked outbox event", ex);
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
    ) {}
}
