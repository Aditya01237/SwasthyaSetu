package com.medicine.appointment.controller;

import com.medicine.appointment.dto.AppointmentReadModelResponse;
import com.medicine.appointment.security.InternalServiceAuthorizer;
import com.medicine.appointment.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/appointments")
public class InternalAppointmentController {

    private final AppointmentService appointmentService;
    private final InternalServiceAuthorizer internalServiceAuthorizer;

    public InternalAppointmentController(AppointmentService appointmentService,
                                         InternalServiceAuthorizer internalServiceAuthorizer) {
        this.appointmentService = appointmentService;
        this.internalServiceAuthorizer = internalServiceAuthorizer;
    }

    @GetMapping("/{id}/read-model")
    public ResponseEntity<AppointmentReadModelResponse> readModelSnapshot(
            @PathVariable("id") Long id,
            @RequestHeader(value = InternalServiceAuthorizer.HEADER, required = false) String internalToken) {
        internalServiceAuthorizer.requireValid(internalToken);
        return ResponseEntity.ok(appointmentService.getReadModelSnapshot(id));
    }
}
