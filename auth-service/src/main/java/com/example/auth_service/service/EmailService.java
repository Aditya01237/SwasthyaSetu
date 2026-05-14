package com.example.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your OTP for Verification – MediCare");
            helper.setFrom(fromEmail);
            helper.setText(buildOtpEmailHtml(otp), true); // true = isHtml

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpEmailHtml(String otp) {
        String html =  """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>
        <body style="margin:0;padding:0;background:#f4f6fb;font-family:'Segoe UI',Arial,sans-serif;">

          <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fb;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="520" cellpadding="0" cellspacing="0"
                       style="background:#ffffff;border-radius:16px;overflow:hidden;
                              box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                  <!-- Header -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#2563eb,#4f46e5);
                               padding:36px 40px;text-align:center;">
                      <div style="width:52px;height:52px;background:rgba(255,255,255,0.15);
                                  border-radius:14px;display:inline-flex;align-items:center;
                                  justify-content:center;font-size:24px;margin-bottom:14px;">
                        🏥
                      </div>
                      <h1 style="margin:0;color:#ffffff;font-size:22px;
                                 font-weight:700;letter-spacing:-0.3px;">
                        MediCare
                      </h1>
                      <p style="margin:6px 0 0;color:rgba(255,255,255,0.7);font-size:13px;">
                        Secure Verification
                      </p>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:40px 40px 32px;">
                      <p style="margin:0 0 8px;font-size:20px;font-weight:700;color:#111827;">
                        Your OTP Code
                      </p>
                      <p style="margin:0 0 28px;font-size:14px;color:#6b7280;line-height:1.6;">
                        Use the code below to complete your verification.
                        This code is valid for <strong style="color:#111827;">5 minutes</strong>.
                      </p>

                      <!-- OTP Box -->
                      <div style="background:#f0f4ff;border:2px dashed #c7d2fe;
                                  border-radius:12px;padding:24px;text-align:center;
                                  margin-bottom:28px;">
                        <p style="margin:0 0 6px;font-size:11px;text-transform:uppercase;
                                  letter-spacing:2px;color:#6b7280;font-weight:600;">
                          One-Time Password
                        </p>
                        <p style="margin:0;font-size:40px;font-weight:800;
                                  letter-spacing:10px;color:#2563eb;">
                          OTP_PLACEHOLDER
                        </p>
                      </div>

                      <!-- Warning -->
                      <div style="background:#fffbeb;border-left:4px solid #f59e0b;
                                  border-radius:6px;padding:12px 16px;margin-bottom:28px;">
                        <p style="margin:0;font-size:12px;color:#92400e;line-height:1.5;">
                          ⚠️ <strong>Never share this OTP</strong> with anyone.
                          Our team will never ask for it.
                        </p>
                      </div>

                      <p style="margin:0;font-size:13px;color:#9ca3af;line-height:1.6;">
                        If you didn't request this, you can safely ignore this email.
                        Your account remains secure.
                      </p>
                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="background:#f9fafb;border-top:1px solid #f3f4f6;
                               padding:20px 40px;text-align:center;">
                      <p style="margin:0;font-size:11px;color:#9ca3af;">
                        © 2026 MediCare · This is an automated email, please do not reply.
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>

        </body>
        </html>
        """;
        return html.replace("OTP_PLACEHOLDER", otp);
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
            helper.setSubject("Appointment Confirmed – SwasthyaSetu");
            helper.setFrom(fromEmail);
            helper.setText(buildAppointmentEmailHtml(
                    patientName, doctorName, doctorSpec,
                    hospitalName, hospitalAddress, appointmentTime, qrToken
            ), true);
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

        // Format the time nicely
        String formattedTime = appointmentTime.replace("T", " at ").substring(0, 19);

        // QR code image via Google Charts API
        String qrImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + qrToken;

        String html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="UTF-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
        </head>
        <body style="margin:0;padding:0;background:#f4f6fb;font-family:'Segoe UI',Arial,sans-serif;">

          <table width="100%" cellpadding="0" cellspacing="0" style="background:#f4f6fb;padding:40px 0;">
            <tr>
              <td align="center">
                <table width="540" cellpadding="0" cellspacing="0"
                       style="background:#ffffff;border-radius:16px;overflow:hidden;
                              box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                  <!-- Header -->
                  <tr>
                    <td style="background:linear-gradient(135deg,#0ea5e9,#2563eb);
                               padding:36px 40px;text-align:center;">
                      <div style="font-size:32px;margin-bottom:12px;">🏥</div>
                      <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:700;">
                        SwasthyaSetu
                      </h1>
                      <p style="margin:6px 0 0;color:rgba(255,255,255,0.75);font-size:13px;">
                        Appointment Confirmation
                      </p>
                    </td>
                  </tr>

                  <!-- Success Banner -->
                  <tr>
                    <td style="background:#f0fdf4;border-bottom:1px solid #bbf7d0;
                               padding:16px 40px;text-align:center;">
                      <p style="margin:0;font-size:14px;color:#15803d;font-weight:600;">
                        ✅ Your appointment has been confirmed!
                      </p>
                    </td>
                  </tr>

                  <!-- Body -->
                  <tr>
                    <td style="padding:36px 40px;">

                      <p style="margin:0 0 20px;font-size:15px;color:#374151;">
                        Hi <strong>PATIENT_NAME</strong>, your appointment is booked. Here are your details:
                      </p>

                      <!-- Details Table -->
                      <table width="100%" cellpadding="0" cellspacing="0"
                             style="background:#f8fafc;border:1px solid #e2e8f0;
                                    border-radius:12px;overflow:hidden;margin-bottom:28px;">
                        <tr>
                          <td style="padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0;font-size:10px;text-transform:uppercase;
                                      letter-spacing:1.5px;color:#94a3b8;font-weight:600;">Doctor</p>
                            <p style="margin:4px 0 0;font-size:15px;color:#1e293b;font-weight:600;">
                              Dr. DOCTOR_NAME
                            </p>
                            <p style="margin:2px 0 0;font-size:12px;color:#64748b;">DOCTOR_SPEC</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:14px 20px;border-bottom:1px solid #e2e8f0;">
                            <p style="margin:0;font-size:10px;text-transform:uppercase;
                                      letter-spacing:1.5px;color:#94a3b8;font-weight:600;">Hospital</p>
                            <p style="margin:4px 0 0;font-size:15px;color:#1e293b;font-weight:600;">
                              HOSPITAL_NAME
                            </p>
                            <p style="margin:2px 0 0;font-size:12px;color:#64748b;">HOSPITAL_ADDRESS</p>
                          </td>
                        </tr>
                        <tr>
                          <td style="padding:14px 20px;">
                            <p style="margin:0;font-size:10px;text-transform:uppercase;
                                      letter-spacing:1.5px;color:#94a3b8;font-weight:600;">
                              Date & Time
                            </p>
                            <p style="margin:4px 0 0;font-size:15px;color:#1e293b;font-weight:600;">
                              📅 APPOINTMENT_TIME
                            </p>
                          </td>
                        </tr>
                      </table>

                      <!-- QR Section -->
                      <div style="text-align:center;background:#f0f4ff;border:2px dashed #c7d2fe;
                                  border-radius:12px;padding:28px;margin-bottom:28px;">
                        <p style="margin:0 0 6px;font-size:11px;text-transform:uppercase;
                                  letter-spacing:2px;color:#6b7280;font-weight:600;">
                          Your Check-in QR Code
                        </p>
                        <img src="QR_IMAGE_URL"
                             alt="QR Code"
                             width="180" height="180"
                             style="margin:12px 0;border-radius:8px;"/>
                        <p style="margin:0;font-size:11px;color:#94a3b8;">
                          Show this QR at the hospital for check-in
                        </p>
                        <p style="margin:6px 0 0;font-size:10px;color:#cbd5e1;
                                  word-break:break-all;">
                          Token: QR_TOKEN
                        </p>
                      </div>

                      <!-- Warning -->
                      <div style="background:#fffbeb;border-left:4px solid #f59e0b;
                                  border-radius:6px;padding:12px 16px;">
                        <p style="margin:0;font-size:12px;color:#92400e;line-height:1.6;">
                          ⚠️ Please arrive <strong>15 minutes early</strong>.
                          Bring a valid ID and this QR code for check-in.
                        </p>
                      </div>

                    </td>
                  </tr>

                  <!-- Footer -->
                  <tr>
                    <td style="background:#f9fafb;border-top:1px solid #f3f4f6;
                               padding:20px 40px;text-align:center;">
                      <p style="margin:0;font-size:11px;color:#9ca3af;">
                        © 2026 SwasthyaSetu · This is an automated email, please do not reply.
                      </p>
                    </td>
                  </tr>

                </table>
              </td>
            </tr>
          </table>

        </body>
        </html>
    """;

        return html
                .replace("PATIENT_NAME", patientName)
                .replace("DOCTOR_NAME", doctorName)
                .replace("DOCTOR_SPEC", doctorSpec)
                .replace("HOSPITAL_NAME", hospitalName)
                .replace("HOSPITAL_ADDRESS", hospitalAddress)
                .replace("APPOINTMENT_TIME", formattedTime)
                .replace("QR_IMAGE_URL", qrImageUrl)
                .replace("QR_TOKEN", qrToken);
    }
}