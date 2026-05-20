package com.medicine.appointment.controller;

import com.medicine.appointment.dto.AppointmentDetailsDoctorResponse;
import com.medicine.appointment.dto.AppointmentDetailsResponse;
import com.medicine.appointment.dto.AppointmentListResponse;
import com.medicine.appointment.dto.AppointmentRequest;
import com.medicine.appointment.dto.AppointmentResponse;
import com.medicine.appointment.dto.CommonResponse;
import com.medicine.appointment.dto.DoctorAppointmentListResponse;
import com.medicine.appointment.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<CommonResponse<AppointmentResponse>> book(@RequestBody AppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.ok(new CommonResponse<>("Appointment registered successfully", response, 200));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentListResponse>> getMyAppointments(@RequestParam String uhid) {
        return ResponseEntity.ok(appointmentService.getAppointments(uhid));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<AppointmentDetailsResponse> getAppointmentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<AppointmentDetailsDoctorResponse> getDoctorAppointmentDetails(
            @PathVariable Long id,
            @RequestParam Long doctorId) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointmentDetails(id, doctorId));
    }

    @GetMapping("/doctor/today")
    public ResponseEntity<List<DoctorAppointmentListResponse>> getDoctorAppointments(@RequestParam Long doctorId) {
        return ResponseEntity.ok(appointmentService.getTodayAppointmentsForDoctor(doctorId));
    }

    @GetMapping("/slots/booked")
    public ResponseEntity<List<String>> getBookedSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getBookedSlotsForDoctor(doctorId, date));
    }
}
