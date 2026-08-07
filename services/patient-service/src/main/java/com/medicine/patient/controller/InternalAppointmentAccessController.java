package com.medicine.patient.controller;

import com.medicine.patient.dto.QrAccessRequest;
import com.medicine.patient.dto.QrAccessResponse;
import com.medicine.patient.security.InternalServiceAuthorizer;
import com.medicine.patient.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/appointments")
public class InternalAppointmentAccessController {

    private final PatientService patientService;
    private final InternalServiceAuthorizer internalServiceAuthorizer;

    public InternalAppointmentAccessController(PatientService patientService,
                                               InternalServiceAuthorizer internalServiceAuthorizer) {
        this.patientService = patientService;
        this.internalServiceAuthorizer = internalServiceAuthorizer;
    }

    @PostMapping("/{appointmentId}/qr-access")
    public ResponseEntity<QrAccessResponse> recordQrAccess(
            @PathVariable Long appointmentId,
            @RequestHeader(value = InternalServiceAuthorizer.HEADER, required = false) String internalToken,
            @RequestBody QrAccessRequest request) {
        internalServiceAuthorizer.requireValid(internalToken);
        return ResponseEntity.ok(patientService.recordQrAccess(appointmentId, request.getDoctorId()));
    }
}
