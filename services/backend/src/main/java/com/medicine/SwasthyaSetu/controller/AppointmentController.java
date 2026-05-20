package com.medicine.SwasthyaSetu.controller;

import com.medicine.SwasthyaSetu.dto.*;
import com.medicine.SwasthyaSetu.service.AppointmentService;
import com.medicine.SwasthyaSetu.service.QrService;
import jakarta.servlet.http.HttpServletRequest;
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
        System.out.println("req recived with uhid : " + uhid);
        List<AppointmentListResponse> appointments = appointmentService.getAppointments(uhid);
        return ResponseEntity.ok(
                new CommonResponse<>("Appointments fetched successfully", appointments, 200).getData()
        );
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<AppointmentDetailsResponse> getAppointmentDetails(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<AppointmentDetailsDoctorResponse> getDoctorAppointmentDetails(
            @PathVariable Long id,
            @RequestParam Long doctorId
    ) {
        return ResponseEntity.ok(
                appointmentService.getDoctorAppointmentDetails(id, doctorId)
        );
    }

    @GetMapping("/doctor/today")
    public ResponseEntity<List<DoctorAppointmentListResponse>> getDoctorAppointments(
            @RequestParam Long doctorId
    ) {
        return ResponseEntity.ok(
                appointmentService.getTodayAppointmentsForDoctor(doctorId)
        );
    }
}
