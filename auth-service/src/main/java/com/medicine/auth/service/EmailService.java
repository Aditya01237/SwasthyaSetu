package com.medicine.auth.service;

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

    public void sendOtpEmail(String toEmail, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP for SwasthyaSetu");
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setText(buildOtpEmailHtml(otp), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Arial,sans-serif;background:#f4f6fb;padding:32px;">
                  <div style="max-width:520px;margin:auto;background:white;border-radius:12px;padding:32px;">
                    <h2 style="margin-top:0;color:#111827;">SwasthyaSetu verification</h2>
                    <p style="color:#4b5563;">Use this OTP to complete your login or email verification.</p>
                    <p style="font-size:36px;font-weight:700;letter-spacing:8px;color:#2563eb;">OTP_PLACEHOLDER</p>
                    <p style="color:#6b7280;font-size:13px;">This code is valid for 5 minutes. Never share it with anyone.</p>
                  </div>
                </body>
                </html>
                """.replace("OTP_PLACEHOLDER", otp);
    }
}
