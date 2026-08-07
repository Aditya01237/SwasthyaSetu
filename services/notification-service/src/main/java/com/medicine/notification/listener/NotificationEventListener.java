package com.medicine.notification.listener;

import com.medicine.notification.dto.AppointmentBookedEvent;
import com.medicine.notification.dto.OtpRequestedEvent;
import com.medicine.notification.dto.PatientRegisteredEvent;
import com.medicine.notification.service.EmailService;
import com.medicine.notification.service.EventIdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final EventIdempotencyService idempotencyService;

    public NotificationEventListener(ObjectMapper objectMapper,
                                     EmailService emailService,
                                     EventIdempotencyService idempotencyService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.idempotencyService = idempotencyService;
    }

    @RabbitListener(queues = "${app.events.queues.appointment-booked}")
    public void handleAppointmentBooked(Message message) {
        process(message, "appointment.booked", payload -> {
            AppointmentBookedEvent event = objectMapper.readValue(payload, AppointmentBookedEvent.class);
            emailService.sendAppointmentConfirmationEmail(event);
            log.info("Processed appointment.booked notification for {}", event.patientEmail());
        });
    }

    @RabbitListener(queues = "${app.events.queues.patient-registered}")
    public void handlePatientRegistered(Message message) {
        process(message, "patient.registered", payload -> {
            PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);
            emailService.sendPatientRegisteredEmail(event);
            log.info("Processed patient.registered notification for {}", event.email());
        });
    }

    @RabbitListener(queues = "${app.events.queues.otp-requested}")
    public void handleOtpRequested(Message message) {
        process(message, "auth.otp-requested", payload -> {
            OtpRequestedEvent event = objectMapper.readValue(payload, OtpRequestedEvent.class);
            emailService.sendOtpEmail(event);
            log.info("Processed auth.otp-requested notification for {}", event.email());
        });
    }

    private void process(Message message, String eventName, ThrowingPayloadConsumer consumer) {
        String eventId = message.getMessageProperties().getMessageId();
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            idempotencyService.processOnce(eventId, () -> {
                try {
                    consumer.accept(payload);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (Exception ex) {
            log.error("Failed to process {} notification; message will be retried", eventName, ex);
            throw new RuntimeException(eventName + " notification failed", ex);
        }
    }

    @FunctionalInterface
    private interface ThrowingPayloadConsumer {
        void accept(String payload) throws Exception;
    }
}
