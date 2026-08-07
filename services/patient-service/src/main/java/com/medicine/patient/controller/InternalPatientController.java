package com.medicine.patient.controller;

import com.medicine.patient.dto.MedicalRecordDTO;
import com.medicine.patient.security.InternalServiceAuthorizer;
import com.medicine.patient.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/patients")
public class InternalPatientController {

    private final PatientService patientService;
    private final InternalServiceAuthorizer internalServiceAuthorizer;

    public InternalPatientController(PatientService patientService,
                                     InternalServiceAuthorizer internalServiceAuthorizer) {
        this.patientService = patientService;
        this.internalServiceAuthorizer = internalServiceAuthorizer;
    }

    @GetMapping("/appointments/{appointmentId}/medical-record")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordForAppointment(
            @PathVariable Long appointmentId,
            @RequestHeader(value = InternalServiceAuthorizer.HEADER, required = false) String internalToken) {
        internalServiceAuthorizer.requireValid(internalToken);
        return patientService.getMedicalRecordForAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
