package com.medicine.appointment.controller;

import com.medicine.appointment.dto.CommonResponse;
import com.medicine.appointment.dto.QrScanRequest;
import com.medicine.appointment.dto.QrScanResponse;
import com.medicine.appointment.exception.AuthorizationException;
import com.medicine.appointment.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @PostMapping("/scan")
    public ResponseEntity<CommonResponse<QrScanResponse>> scan(
            @RequestHeader(USER_ID_HEADER) String authenticatedDoctorId,
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestBody QrScanRequest request) {
        requireDoctor(role);
        return ResponseEntity.ok(new CommonResponse<>(
                "Qr Scan",
                qrService.scan(request, parseDoctorId(authenticatedDoctorId)),
                200
        ));
    }

    private void requireDoctor(String role) {
        if (role == null || !"DOCTOR".equalsIgnoreCase(role)) {
            throw new AuthorizationException("QR scanning requires DOCTOR access");
        }
    }

    private Long parseDoctorId(String authenticatedDoctorId) {
        try {
            return Long.parseLong(authenticatedDoctorId);
        } catch (NumberFormatException ex) {
            throw new AuthorizationException("Invalid authenticated doctor identity");
        }
    }
}
