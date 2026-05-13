package com.medicine.patient.controller;

import com.medicine.patient.dto.QrAccessRequest;
import com.medicine.patient.dto.QrAccessResponse;
import com.medicine.patient.dto.MedicalRecordDTO;
import com.medicine.patient.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/patients")
public class InternalPatientController {

    private final PatientService patientService;

    public InternalPatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("/{patientId}/qr-access")
    public ResponseEntity<QrAccessResponse> recordQrAccess(
            @PathVariable Long patientId,
            @RequestBody QrAccessRequest request
    ) {
        return ResponseEntity.ok(patientService.recordQrAccess(
                patientId,
                request.getAppointmentId(),
                request.getDoctorId()
        ));
    }

    @GetMapping("/appointments/{appointmentId}/medical-record")
    public ResponseEntity<MedicalRecordDTO> getMedicalRecordForAppointment(@PathVariable Long appointmentId) {
        return patientService.getMedicalRecordForAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
