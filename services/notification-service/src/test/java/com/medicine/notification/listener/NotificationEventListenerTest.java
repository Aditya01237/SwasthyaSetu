package com.medicine.notification.listener;

import com.medicine.notification.dto.AppointmentBookedEvent;
import com.medicine.notification.dto.OtpRequestedEvent;
import com.medicine.notification.dto.PatientRegisteredEvent;
import com.medicine.notification.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationEventListenerTest {

    private RecordingEmailService emailService;
    private NotificationEventListener listener;

    @BeforeEach
    void setUp() {
        emailService = new RecordingEmailService();
        listener = new NotificationEventListener(new ObjectMapper(), emailService);
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

        listener.handleAppointmentBooked(payload);

        assertThat(emailService.appointmentBookedEvent.patientEmail()).isEqualTo("patient@example.com");
        assertThat(emailService.appointmentBookedEvent.qrToken()).isEqualTo("qr-token");
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

        listener.handlePatientRegistered(payload);

        assertThat(emailService.patientRegisteredEvent.uhid()).isEqualTo("UHID123");
        assertThat(emailService.patientRegisteredEvent.email()).isEqualTo("patient@example.com");
    }

    @Test
    void handlesOtpRequestedPayload() {
        String payload = """
                {
                  "email": "patient@example.com",
                  "otp": "123456"
                }
                """;

        listener.handleOtpRequested(payload);

        assertThat(emailService.otpRequestedEvent.email()).isEqualTo("patient@example.com");
        assertThat(emailService.otpRequestedEvent.otp()).isEqualTo("123456");
    }

    private static class RecordingEmailService extends EmailService {

        private AppointmentBookedEvent appointmentBookedEvent;
        private PatientRegisteredEvent patientRegisteredEvent;
        private OtpRequestedEvent otpRequestedEvent;

        RecordingEmailService() {
            super(null, "");
        }

        @Override
        public void sendAppointmentConfirmationEmail(AppointmentBookedEvent event) {
            this.appointmentBookedEvent = event;
        }

        @Override
        public void sendPatientRegisteredEmail(PatientRegisteredEvent event) {
            this.patientRegisteredEvent = event;
        }

        @Override
        public void sendOtpEmail(OtpRequestedEvent event) {
            this.otpRequestedEvent = event;
        }
    }
}
