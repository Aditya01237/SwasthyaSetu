package com.medicine.appointment.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendAppointmentConfirmationEmail(String toEmail,
                                                 String patientName,
                                                 String doctorName,
                                                 String doctorSpec,
                                                 String hospitalName,
                                                 String hospitalAddress,
                                                 String appointmentTime,
                                                 String qrToken) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Appointment Confirmed - SwasthyaSetu");
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setText(buildAppointmentEmailHtml(
                    patientName, doctorName, doctorSpec, hospitalName, hospitalAddress, appointmentTime, qrToken
            ), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send appointment email", e);
        }
    }

    private String buildAppointmentEmailHtml(String patientName,
                                             String doctorName,
                                             String doctorSpec,
                                             String hospitalName,
                                             String hospitalAddress,
                                             String appointmentTime,
                                             String qrToken) {
        String formattedTime = appointmentTime.replace("T", " at ");
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + qrToken;

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f6fb;padding:32px;">
                  <div style="max-width:560px;margin:auto;background:white;border-radius:12px;padding:32px;">
                    <h2 style="margin-top:0;color:#111827;">Appointment confirmed</h2>
                    <p>Hi <strong>PATIENT_NAME</strong>, your appointment is booked.</p>
                    <p><strong>Doctor:</strong> Dr. DOCTOR_NAME, DOCTOR_SPEC</p>
                    <p><strong>Hospital:</strong> HOSPITAL_NAME, HOSPITAL_ADDRESS</p>
                    <p><strong>Time:</strong> APPOINTMENT_TIME</p>
                    <img src="QR_IMAGE_URL" alt="QR code" width="180" height="180"/>
                    <p style="font-size:12px;color:#6b7280;">Show this QR code at your appointment.</p>
                  </div>
                </body>
                </html>
                """
                .replace("PATIENT_NAME", patientName)
                .replace("DOCTOR_NAME", doctorName)
                .replace("DOCTOR_SPEC", doctorSpec)
                .replace("HOSPITAL_NAME", hospitalName)
                .replace("HOSPITAL_ADDRESS", hospitalAddress)
                .replace("APPOINTMENT_TIME", formattedTime)
                .replace("QR_IMAGE_URL", qrImageUrl);
    }
}
