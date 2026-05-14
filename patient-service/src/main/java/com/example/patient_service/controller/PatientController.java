package com.example.patient_service.controller;

import com.example.patient_service.dto.*;

import com.example.patient_service.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<PatientResponse>> registerPatient(@Valid @RequestBody PatientRegisterRequest request){
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.ok(
                new CommonResponse<>("Patient registered successfully", response, 200)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<CommonResponse<PatientDetailsResponse>> getPatientDetails(HttpServletRequest request) {

        String uhid = (String) request.getAttribute("uhid");

        if (uhid == null) {
            throw new RuntimeException("Unauthorized: UHID missing");
        }

        PatientDetailsRequest patientDetailsRequest = new PatientDetailsRequest();
        patientDetailsRequest.setUhid(uhid);

        PatientDetailsResponse response = patientService.getPatientDetails(patientDetailsRequest);

        return ResponseEntity.ok(
                new CommonResponse<>("Patient details fetched successfully", response, 200)
        );
    }

    @GetMapping("/qr-audit")
    public ResponseEntity<CommonResponse<List<AuditLogResponse>>> getAuditLogs(HttpServletRequest request) {

        String uhid = (String) request.getAttribute("uhid");

        if (uhid == null) {
            throw new RuntimeException("Unauthorized: UHID missing");
        }

        List<AuditLogResponse> logs = patientService.getAuditLogs(uhid);

        return ResponseEntity.ok(
                new CommonResponse<>("Audit logs fetched successfully", logs, 200)
        );
    }

    @PostMapping("/upload-prescription/{appointmentId}")
    public ResponseEntity<?> uploadPrescription(
            HttpServletRequest request,
            @PathVariable Long appointmentId,
            @RequestParam("file") MultipartFile file
    ) {
        String uhid = (String) request.getAttribute("uhid");

        if (uhid == null) {
            throw new RuntimeException("Unauthorized: UHID missing");
        }

        return ResponseEntity.ok(
                patientService.processPrescription(uhid, appointmentId, file)
        );
    }
    @PostMapping("/internal/{patientId}/scan-access")
    public ResponseEntity<List<MedicalRecordDTO>> scanAccess(
            @PathVariable Long patientId,
            @RequestParam Long doctorId
    ) {
        return ResponseEntity.ok(patientService.processQrScanAccess(patientId, doctorId));
    }
}
