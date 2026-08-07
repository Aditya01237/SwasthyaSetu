package com.medicine.hospital.security;

import com.medicine.hospital.exception.AuthorizationException;
import org.springframework.stereotype.Component;

@Component
public class HospitalAuthorization {

    public void requireAdmin(String role) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new AuthorizationException("ADMIN access required");
        }
    }

    public void requireHospitalWriteAccess(String role, String authenticatedUserId, String hospitalId) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return;
        }

        if ("HOSPITAL_ADMIN".equalsIgnoreCase(role)
                && authenticatedUserId != null
                && authenticatedUserId.equals(hospitalId)) {
            return;
        }

        throw new AuthorizationException("ADMIN or matching HOSPITAL_ADMIN access required");
    }
}
