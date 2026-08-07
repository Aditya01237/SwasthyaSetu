package com.medicine.notification.listener;

import com.medicine.notification.dto.AppointmentBookedEvent;
import com.medicine.notification.dto.OtpRequestedEvent;
import com.medicine.notification.dto.PatientRegisteredEvent;
import com.medicine.notification.service.EmailService;
import com.medicine.notification.service.EventIdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationEventListenerTest {

    private EmailService emailService;
    private EventIdempotencyService idempotencyService;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        emailService = mock(EmailService.class);
        idempotencyService = mock(EventIdempotencyService.class);
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(1);
            action.run();
            return null;
        }).when(idempotencyService).processOnce(any(), any(Runnable.class));

        listener = new NotificationEventListener(new ObjectMapper(), emailService, idempotencyService);
    }

    @Test
    void handlesAppointmentBookedPayload() {
        String payload = """
                {
                  "patientEmail": "patient@example.com",
                  "patientName": "Asha",
                  "doctorName": "Mehta",
                  "doctorSpec": "Cardiology",
                  "hospitalName": "City Hospital",
                  "hospitalAddress": "MG Road",
                  "appointmentTime": "2026-05-12T10:30",
                  "qrToken": "qr-token"
                }
                """;

        listener.handleAppointmentBooked(message("appointment-1", payload));

        ArgumentCaptor<AppointmentBookedEvent> captor = ArgumentCaptor.forClass(AppointmentBookedEvent.class);
        verify(emailService).sendAppointmentConfirmationEmail(captor.capture());
        assertThat(captor.getValue().patientEmail()).isEqualTo("patient@example.com");
        assertThat(captor.getValue().qrToken()).isEqualTo("qr-token");
    }

    @Test
    void handlesPatientRegisteredPayload() {
        String payload = """
                {
                  "uhid": "UHID123",
                  "name": "Asha",
                  "email": "patient@example.com"
                }
                """;

        listener.handlePatientRegistered(message("patient-1", payload));

        ArgumentCaptor<PatientRegisteredEvent> captor = ArgumentCaptor.forClass(PatientRegisteredEvent.class);
        verify(emailService).sendPatientRegisteredEmail(captor.capture());
        assertThat(captor.getValue().uhid()).isEqualTo("UHID123");
        assertThat(captor.getValue().email()).isEqualTo("patient@example.com");
    }

    @Test
    void handlesOtpRequestedPayload() {
        String payload = """
                {
                  "email": "patient@example.com",
                  "otp": "123456"
                }
                """;

        listener.handleOtpRequested(message("otp-1", payload));

        ArgumentCaptor<OtpRequestedEvent> captor = ArgumentCaptor.forClass(OtpRequestedEvent.class);
        verify(emailService).sendOtpEmail(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("patient@example.com");
        assertThat(captor.getValue().otp()).isEqualTo("123456");
    }

    private Message message(String id, String payload) {
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(id);
        return new Message(payload.getBytes(StandardCharsets.UTF_8), properties);
    }
}
