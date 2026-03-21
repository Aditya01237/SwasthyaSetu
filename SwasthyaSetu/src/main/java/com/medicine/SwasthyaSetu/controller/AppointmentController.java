package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.AppointmentRequest;
import com.medicine.SwasthyaSetu.dto.AppointmentResponse;
import com.medicine.SwasthyaSetu.service.AppointmentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {
    private final AppointmentService appointmentService;
    public AppointmentController(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public AppointmentResponse book(@RequestBody AppointmentRequest appointmentRequest){
        return appointmentService.bookAppointment(appointmentRequest);
    }
}
