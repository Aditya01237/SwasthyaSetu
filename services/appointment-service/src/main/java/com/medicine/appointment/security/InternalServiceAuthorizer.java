package com.medicine.appointment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalServiceAuthorizer {

    public static final String HEADER = "X-Internal-Service-Token";

    private final String expectedToken;

    public InternalServiceAuthorizer(@Value("${app.internal.service-token}") String expectedToken) {
        if (expectedToken == null || expectedToken.isBlank()) {
            throw new IllegalStateException("Internal service token must be configured");
        }
        this.expectedToken = expectedToken;
    }

    public void requireValid(String suppliedToken) {
        if (suppliedToken == null || !MessageDigest.isEqual(
                expectedToken.getBytes(StandardCharsets.UTF_8),
                suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid internal service credentials");
        }
    }
}
