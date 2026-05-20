package com.medicine.notification.service;

import com.medicine.notification.dto.AppointmentBookedEvent;
import com.medicine.notification.dto.OtpRequestedEvent;
import com.medicine.notification.dto.PatientRegisteredEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    /** Envelope From; required by many SMTP servers. Not the same as SMTP auth username. */
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.notification.from:noreply@swasthyasetu.local}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendAppointmentConfirmationEmail(AppointmentBookedEvent event) {
        if (isBlank(event.patientEmail())) {
            log.warn("Skipping appointment notification because patient email is missing");
            return;
        }
        sendHtmlEmail(event.patientEmail(), "Appointment Confirmed – SwasthyaSetu", buildAppointmentEmailHtml(event));
    }

    public void sendPatientRegisteredEmail(PatientRegisteredEvent event) {
        if (isBlank(event.email())) {
            log.warn("Skipping patient registration notification for {} because email is missing", event.uhid());
            return;
        }
        sendHtmlEmail(event.email(), "Welcome to SwasthyaSetu", buildPatientRegisteredEmailHtml(event));
    }

    public void sendOtpEmail(OtpRequestedEvent event) {
        if (isBlank(event.email())) {
            log.warn("Skipping OTP notification because email is missing");
            return;
        }
        sendHtmlEmail(event.email(), "Your OTP for SwasthyaSetu", buildOtpEmailHtml(event.otp()));
    }

    private void sendHtmlEmail(String toEmail, String subject, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send notification email", ex);
        }
    }

    // ─── HTML Builders ────────────────────────────────────────────────────────

    private String buildAppointmentEmailHtml(AppointmentBookedEvent event) {
        String formattedTime = value(event.appointmentTime()).replace("T", " at ");
        if (formattedTime.length() > 19)
            formattedTime = formattedTime.substring(0, 19);
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + value(event.qrToken());

        return wrapInShell("""
                <h2 style="margin:0 0 4px;font-size:20px;font-weight:700;color:#0d1b4b;">Appointment Confirmed</h2>
                <p style="margin:0 0 24px;font-size:14px;color:#6b7a99;">
                  Hi <strong>PATIENT_NAME</strong>, your appointment has been booked successfully.
                </p>

                <table width="100%" cellpadding="0" cellspacing="0"
                       style="border:1px solid #e2e8f0;border-radius:8px;border-collapse:separate;">
                  <tr>
                    <td style="padding:14px 18px;border-bottom:1px solid #e2e8f0;">
                      <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                                  letter-spacing:.5px;color:#1a3faa;margin-bottom:4px;">Doctor</div>
                      <div style="font-size:15px;font-weight:600;color:#0d1b4b;">Dr. DOCTOR_NAME</div>
                      <div style="font-size:13px;color:#00b8a0;margin-top:2px;">DOCTOR_SPEC</div>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:14px 18px;border-bottom:1px solid #e2e8f0;">
                      <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                                  letter-spacing:.5px;color:#1a3faa;margin-bottom:4px;">Hospital</div>
                      <div style="font-size:15px;font-weight:600;color:#0d1b4b;">HOSPITAL_NAME</div>
                      <div style="font-size:13px;color:#6b7a99;margin-top:2px;">HOSPITAL_ADDRESS</div>
                    </td>
                  </tr>
                  <tr>
                    <td style="padding:14px 18px;">
                      <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                                  letter-spacing:.5px;color:#1a3faa;margin-bottom:4px;">Date &amp; Time</div>
                      <div style="font-size:15px;font-weight:600;color:#0d1b4b;">APPOINTMENT_TIME</div>
                    </td>
                  </tr>
                </table>

                <div style="text-align:center;margin:24px 0 0;padding:20px;
                            border:1px solid #e2e8f0;border-radius:8px;">
                  <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                              letter-spacing:.5px;color:#1a3faa;margin-bottom:14px;">Check-in QR Code</div>
                  <img src="QR_IMAGE_URL" alt="QR Code" width="180" height="180"
                       style="display:block;margin:0 auto;border-radius:4px;"/>
                  <p style="margin:12px 0 0;font-size:12px;color:#9aabbf;">
                    Show this QR code at the reception desk.
                  </p>
                </div>
                """)
                .replace("PATIENT_NAME", value(event.patientName()))
                .replace("DOCTOR_NAME", value(event.doctorName()))
                .replace("DOCTOR_SPEC", value(event.doctorSpec()))
                .replace("HOSPITAL_NAME", value(event.hospitalName()))
                .replace("HOSPITAL_ADDRESS", value(event.hospitalAddress()))
                .replace("APPOINTMENT_TIME", formattedTime)
                .replace("QR_IMAGE_URL", qrImageUrl);
    }

    private String buildPatientRegisteredEmailHtml(PatientRegisteredEvent event) {
        return wrapInShell("""
                <h2 style="margin:0 0 4px;font-size:20px;font-weight:700;color:#0d1b4b;">Welcome to SwasthyaSetu</h2>
                <p style="margin:0 0 24px;font-size:14px;color:#6b7a99;">
                  Hi <strong>PATIENT_NAME</strong>, your patient profile has been created successfully.
                </p>

                <div style="background:#f4f8ff;border:1px solid #d0dcf5;border-radius:8px;
                            padding:20px;margin:0 0 20px;text-align:center;">
                  <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                              letter-spacing:.5px;color:#1a3faa;margin-bottom:10px;">
                    Your Unique Health ID (UHID)
                  </div>
                  <div style="font-size:30px;font-weight:800;letter-spacing:6px;
                              color:#0d1b4b;font-family:'Courier New',monospace;">
                    UHID_VALUE
                  </div>
                </div>

                <table width="100%" cellpadding="0" cellspacing="0"
                       style="background:#fffbeb;border:1px solid #fde68a;
                              border-radius:8px;border-collapse:separate;">
                  <tr>
                    <td style="padding:14px 18px;font-size:13px;color:#7a5500;line-height:1.6;">
                      Keep your UHID safe. You will need it to log in, book appointments,
                      and access your health records. Do not share it with anyone.
                    </td>
                  </tr>
                </table>
                """)
                .replace("PATIENT_NAME", value(event.name()))
                .replace("UHID_VALUE", value(event.uhid()));
    }

    private String buildOtpEmailHtml(String otp) {
        return wrapInShell("""
                <h2 style="margin:0 0 4px;font-size:20px;font-weight:700;color:#0d1b4b;">Verification Code</h2>
                <p style="margin:0 0 24px;font-size:14px;color:#6b7a99;">
                  Use the code below to complete your login or verification.
                </p>

                <div style="background:#f4f8ff;border:1px solid #d0dcf5;border-radius:8px;
                            padding:28px 20px;margin:0 0 20px;text-align:center;">
                  <div style="font-size:11px;font-weight:600;text-transform:uppercase;
                              letter-spacing:.5px;color:#1a3faa;margin-bottom:14px;">
                    One-Time Password
                  </div>
                  <div style="font-size:44px;font-weight:800;letter-spacing:16px;
                              color:#0d1b4b;font-family:'Courier New',monospace;">
                    OTP_PLACEHOLDER
                  </div>
                </div>

                <table width="100%" cellpadding="0" cellspacing="0"
                       style="background:#fff2f2;border:1px solid #fecaca;
                              border-radius:8px;border-collapse:separate;">
                  <tr>
                    <td style="padding:14px 18px;font-size:13px;color:#8b1a1a;line-height:1.6;">
                      This code expires in <strong>5 minutes</strong>.
                      SwasthyaSetu will never ask for your OTP. Do not share it with anyone.
                    </td>
                  </tr>
                </table>
                """)
                .replace("OTP_PLACEHOLDER", value(otp));
    }

    // ─── Shared Shell ─────────────────────────────────────────────────────────

    private String wrapInShell(String bodyContent) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width,initial-scale=1.0"/>
                  <title>SwasthyaSetu</title>
                </head>
                <body style="margin:0;padding:0;background:#f0f4f8;
                             font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;">

                  <table width="100%" cellpadding="0" cellspacing="0" style="padding:40px 16px;background:#f0f4f8;">
                    <tr><td align="center">
                      <table width="100%" cellpadding="0" cellspacing="0" style="max-width:520px;">

                        <!-- Header -->
                        <tr>
                          <td align="center"
                              style="background:linear-gradient(135deg,#0d1b4b,#1a3faa 60%,#00b8a0);
                                     border-radius:10px 10px 0 0;padding:28px 32px;">
                            <div style="font-size:22px;font-weight:800;color:#ffffff;letter-spacing:-.2px;">
                              Swasthya<span style="color:#7fffd4;">Setu</span>
                            </div>
                            <div style="font-size:12px;color:rgba(255,255,255,0.65);
                                        margin-top:4px;letter-spacing:.4px;">
                              Connecting You to Better Care
                            </div>
                          </td>
                        </tr>

                        <!-- Body -->
                        <tr>
                          <td style="background:#ffffff;padding:28px 32px;
                                     border-left:1px solid #e2e8f0;border-right:1px solid #e2e8f0;">
                            BODY_CONTENT
                          </td>
                        </tr>

                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;border:1px solid #e2e8f0;border-top:none;
                                     border-radius:0 0 10px 10px;padding:18px 32px;text-align:center;">
                            <p style="margin:0;font-size:12px;color:#9aabbf;line-height:1.6;">
                              This is an automated message — please do not reply.<br/>
                              &copy; 2026 SwasthyaSetu. All rights reserved.
                            </p>
                          </td>
                        </tr>

                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """
                .replace("BODY_CONTENT", bodyContent);
    }

    // ─── Utilities ────────────────────────────────────────────────────────────

    private String value(String input) {
        return input == null ? "" : input;
    }

    private boolean isBlank(String input) {
        return input == null || input.isBlank();
    }
}