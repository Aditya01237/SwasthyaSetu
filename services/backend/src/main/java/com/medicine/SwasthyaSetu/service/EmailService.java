package com.medicine.SwasthyaSetu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

@Service
public class EmailService {

    private static final String LOGO_CONTENT_ID = "swasthya-logo";
    private static final String LOGO_SRC = "cid:" + LOGO_CONTENT_ID;
    private static final ClassPathResource LOGO_RESOURCE = new ClassPathResource("email/logo.png");

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your OTP for SwasthyaSetu");
            if (!isBlank(fromEmail)) {
                helper.setFrom(fromEmail);
            }
            helper.setText(buildOtpEmailHtml(otp), true);
            helper.addInline(LOGO_CONTENT_ID, LOGO_RESOURCE, "image/png");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f6fb;padding:32px;margin:0;">
                  <div style="max-width:760px;margin:auto;background:#ffffff;border-radius:12px;padding:48px 56px;">
                    <img src="LOGO_URL" alt="SwasthyaSetu" width="72" height="72" style="display:block;border:0;border-radius:16px;margin:0 0 28px;"/>
                    <h2 style="margin:0 0 32px;color:#111827;font-size:28px;font-weight:700;">SwasthyaSetu verification</h2>
                    <p style="margin:0 0 64px;color:#4b5563;font-size:18px;line-height:1.5;">Use this OTP to complete your login or email verification.</p>
                    <p style="margin:0 0 64px;font-size:48px;font-weight:700;letter-spacing:14px;color:#2563eb;">OTP_PLACEHOLDER</p>
                    <p style="margin:0;color:#6b7280;font-size:17px;line-height:1.5;">This code is valid for 5 minutes. Never share it with anyone.</p>
                  </div>
                </body>
                </html>
                """
                .replace("LOGO_URL", LOGO_SRC)
                .replace("OTP_PLACEHOLDER", value(otp));
    }

    public void sendAppointmentConfirmationEmail(
            String toEmail,
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
            if (!isBlank(fromEmail)) {
                helper.setFrom(fromEmail);
            }
            helper.setText(buildAppointmentEmailHtml(
                    patientName, doctorName, doctorSpec,
                    hospitalName, hospitalAddress, appointmentTime, qrToken
            ), true);
            helper.addInline(LOGO_CONTENT_ID, LOGO_RESOURCE, "image/png");
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send appointment email", e);
        }
    }

    private String buildAppointmentEmailHtml(
            String patientName,
            String doctorName,
            String doctorSpec,
            String hospitalName,
            String hospitalAddress,
            String appointmentTime,
            String qrToken) {

        String formattedTime = value(appointmentTime).replace("T", " at ");
        if (formattedTime.length() > 19) {
            formattedTime = formattedTime.substring(0, 19);
        }
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=240x240&data=" + value(qrToken);

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f6fb;padding:32px;margin:0;">
                  <div style="max-width:760px;margin:auto;background:#ffffff;border-radius:12px;padding:48px 56px;">
                    <img src="LOGO_URL" alt="SwasthyaSetu" width="72" height="72" style="display:block;border:0;border-radius:16px;margin:0 0 28px;"/>
                    <h2 style="margin:0 0 32px;color:#111827;font-size:28px;font-weight:700;">Appointment confirmed</h2>
                    <p style="margin:0 0 28px;color:#202124;font-size:18px;line-height:1.5;">Hi <strong>PATIENT_NAME</strong>, your appointment is booked.</p>
                    <p style="margin:0 0 24px;color:#202124;font-size:18px;line-height:1.5;"><strong>Doctor:</strong> Dr. DOCTOR_NAME, DOCTOR_SPEC</p>
                    <p style="margin:0 0 24px;color:#202124;font-size:18px;line-height:1.5;"><strong>Hospital:</strong> HOSPITAL_NAME, HOSPITAL_ADDRESS</p>
                    <p style="margin:0 0 24px;color:#202124;font-size:18px;line-height:1.5;"><strong>Time:</strong> APPOINTMENT_TIME</p>
                    <img src="QR_IMAGE_URL" alt="QR code" width="240" height="240" style="display:block;margin:24px 0 32px;"/>
                    <p style="margin:0;color:#6b7280;font-size:17px;line-height:1.5;">Show this QR code at your appointment.</p>
                  </div>
                </body>
                </html>
                """;

        return html
                .replace("PATIENT_NAME", value(patientName))
                .replace("DOCTOR_NAME", value(doctorName))
                .replace("DOCTOR_SPEC", value(doctorSpec))
                .replace("HOSPITAL_NAME", value(hospitalName))
                .replace("HOSPITAL_ADDRESS", value(hospitalAddress))
                .replace("APPOINTMENT_TIME", formattedTime)
                .replace("QR_IMAGE_URL", qrImageUrl)
                .replace("LOGO_URL", LOGO_SRC);
    }

    private String value(String input) {
        return input == null ? "" : input;
    }

    private boolean isBlank(String input) {
        return input == null || input.isBlank();
    }
}
