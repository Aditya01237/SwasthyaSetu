package com.medicine.patient.controller;

import com.medicine.patient.dto.QrAccessRequest;
import com.medicine.patient.dto.QrAccessResponse;
import com.medicine.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/appointments")
public class InternalAppointmentAccessController {

    private final PatientService patientService;

    public InternalAppointmentAccessController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/{appointmentId}/qr-access")
    public ResponseEntity<QrAccessResponse> recordQrAccess(
            @PathVariable Long appointmentId,
            @RequestBody QrAccessRequest request
    ) {
        return ResponseEntity.ok(patientService.recordQrAccess(appointmentId, request.getDoctorId()));
    }
}
