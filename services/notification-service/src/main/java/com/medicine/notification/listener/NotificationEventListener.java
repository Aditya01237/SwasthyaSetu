package com.medicine.notification.listener;

import com.medicine.notification.dto.AppointmentBookedEvent;
import com.medicine.notification.dto.OtpRequestedEvent;
import com.medicine.notification.dto.PatientRegisteredEvent;
import com.medicine.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    public NotificationEventListener(ObjectMapper objectMapper, EmailService emailService) {
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.events.queues.appointment-booked}")
    public void handleAppointmentBooked(String payload) {
        try {
            AppointmentBookedEvent event = objectMapper.readValue(payload, AppointmentBookedEvent.class);
            emailService.sendAppointmentConfirmationEmail(event);
            log.info("Processed appointment.booked notification for {}", event.patientEmail());
        } catch (Exception ex) {
            log.error("Failed to process appointment.booked notification", ex);
        }
    }

    @RabbitListener(queues = "${app.events.queues.patient-registered}")
    public void handlePatientRegistered(String payload) {
        try {
            PatientRegisteredEvent event = objectMapper.readValue(payload, PatientRegisteredEvent.class);
            emailService.sendPatientRegisteredEmail(event);
            log.info("Processed patient.registered notification for {}", event.email());
        } catch (Exception ex) {
            log.error("Failed to process patient.registered notification", ex);
        }
    }

    @RabbitListener(queues = "${app.events.queues.otp-requested}")
    public void handleOtpRequested(String payload) {
        try {
            OtpRequestedEvent event = objectMapper.readValue(payload, OtpRequestedEvent.class);
            emailService.sendOtpEmail(event);
            log.info("Processed auth.otp-requested notification for {}", event.email());
        } catch (Exception ex) {
            log.error("Failed to process auth.otp-requested notification", ex);
        }
    }
}
