package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<CommonResponse<AppointmentResponse>> book(@RequestBody AppointmentRequest appointmentRequest) {
        AppointmentResponse response = appointmentService.bookAppointment(appointmentRequest);
        return ResponseEntity.ok(
                new CommonResponse<>("Appointment registered successfully", response, 200)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentListResponse>> getMyAppointments(@RequestParam String uhid){
        return ResponseEntity.ok(appointmentService.getAppointments(uhid));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<AppointmentDetailsResponse> getAppointmentDetails(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id));
    }
}
