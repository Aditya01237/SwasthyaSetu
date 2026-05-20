package com.medicine.notification.dto;

public record OtpRequestedEvent(
        String email,
        String otp
) {
}
