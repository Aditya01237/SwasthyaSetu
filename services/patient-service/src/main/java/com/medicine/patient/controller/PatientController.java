package com.medicine.patient.controller;

import com.medicine.patient.dto.AuditLogResponse;
import com.medicine.patient.dto.CommonResponse;
import com.medicine.patient.dto.PatientDetailsResponse;
import com.medicine.patient.dto.PatientRegisterRequest;
import com.medicine.patient.dto.PatientResponse;
import com.medicine.patient.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<PatientResponse>> registerPatient(
            @Valid @RequestBody PatientRegisterRequest request
    ) {
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.ok(new CommonResponse<>("Patient registered successfully", response, 200));
    }

    @GetMapping("/history")
    public ResponseEntity<CommonResponse<PatientDetailsResponse>> getPatientDetails(
            @RequestHeader(USER_ID_HEADER) String uhid,
            @RequestHeader(USER_ROLE_HEADER) String role
    ) {
        requirePatient(uhid, role);
        PatientDetailsResponse response = patientService.getPatientDetails(uhid);
        return ResponseEntity.ok(new CommonResponse<>("Patient details fetched successfully", response, 200));
    }

    @GetMapping("/qr-audit")
    public ResponseEntity<CommonResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestHeader(USER_ID_HEADER) String uhid,
            @RequestHeader(USER_ROLE_HEADER) String role
    ) {
        requirePatient(uhid, role);
        List<AuditLogResponse> logs = patientService.getAuditLogs(uhid);
        return ResponseEntity.ok(new CommonResponse<>("Audit logs fetched successfully", logs, 200));
    }

    @PostMapping("/upload-prescription/{appointmentId}")
    public ResponseEntity<?> uploadPrescription(
            @RequestHeader(USER_ID_HEADER) String uhid,
            @RequestHeader(USER_ROLE_HEADER) String role,
            @PathVariable Long appointmentId,
            @RequestParam("file") MultipartFile file
    ) {
        requirePatient(uhid, role);
        return ResponseEntity.ok(patientService.processPrescription(uhid, appointmentId, file));
    }

    private void requirePatient(String uhid, String role) {
        if (uhid == null || uhid.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated patient identity is missing");
        }
        if (!"PATIENT".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PATIENT access required");
        }
    }
}
