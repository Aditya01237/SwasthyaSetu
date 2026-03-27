package com.medicine.SwasthyaSetu.controller;
import com.medicine.SwasthyaSetu.Entity.Appointment;
import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.repository.AppointmentRepository;
import com.medicine.SwasthyaSetu.service.AppointmentService;
import com.medicine.SwasthyaSetu.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService, AppointmentService appointmentService,
                             AppointmentRepository appointmentRepository){
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

        List<AuditLogResponse> logs = patientService.getAuditLogs(uhid);

        return ResponseEntity.ok(
                new CommonResponse<>("Audit logs fetched successfully", logs, 200)
        );
    }
}
