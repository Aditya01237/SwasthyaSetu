package com.medicine.appointment.controller;

import com.medicine.appointment.dto.AppointmentReadModelResponse;
import com.medicine.appointment.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/appointments")
public class InternalAppointmentController {

    private final AppointmentService appointmentService;

    public InternalAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{id}/read-model")
    public ResponseEntity<AppointmentReadModelResponse> readModelSnapshot(@PathVariable("id") Long id) {
        return ResponseEntity.ok(appointmentService.getReadModelSnapshot(id));
    }
}
