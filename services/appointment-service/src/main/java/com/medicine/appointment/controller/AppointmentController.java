package com.medicine.appointment.controller;

import com.medicine.appointment.dto.AppointmentDetailsDoctorResponse;
import com.medicine.appointment.dto.AppointmentDetailsResponse;
import com.medicine.appointment.dto.AppointmentListResponse;
import com.medicine.appointment.dto.AppointmentRequest;
import com.medicine.appointment.dto.AppointmentResponse;
import com.medicine.appointment.dto.CommonResponse;
import com.medicine.appointment.dto.DoctorAppointmentListResponse;
import com.medicine.appointment.exception.AuthorizationException;
import com.medicine.appointment.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/book")
    public ResponseEntity<CommonResponse<AppointmentResponse>> book(
            @RequestHeader(USER_ID_HEADER) String patientUhid,
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestBody AppointmentRequest request) {
        requireRole(role, "PATIENT");
        AppointmentResponse response = appointmentService.bookAppointment(request, patientUhid);
        return ResponseEntity.ok(new CommonResponse<>("Appointment registered successfully", response, 200));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AppointmentListResponse>> getMyAppointments(
            @RequestHeader(USER_ID_HEADER) String patientUhid,
            @RequestHeader(USER_ROLE_HEADER) String role) {
        requireRole(role, "PATIENT");
        return ResponseEntity.ok(appointmentService.getAppointments(patientUhid));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<AppointmentDetailsResponse> getAppointmentDetails(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) String patientUhid,
            @RequestHeader(USER_ROLE_HEADER) String role) {
        requireRole(role, "PATIENT");
        return ResponseEntity.ok(appointmentService.getAppointmentDetails(id, patientUhid));
    }

    @GetMapping("/doctor/{id}")
    public ResponseEntity<AppointmentDetailsDoctorResponse> getDoctorAppointmentDetails(
            @PathVariable Long id,
            @RequestHeader(USER_ID_HEADER) String authenticatedDoctorId,
            @RequestHeader(USER_ROLE_HEADER) String role) {
        requireRole(role, "DOCTOR");
        return ResponseEntity.ok(
                appointmentService.getDoctorAppointmentDetails(id, parseDoctorId(authenticatedDoctorId))
        );
    }

    @GetMapping("/doctor/today")
    public ResponseEntity<List<DoctorAppointmentListResponse>> getDoctorAppointments(
            @RequestHeader(USER_ID_HEADER) String authenticatedDoctorId,
            @RequestHeader(USER_ROLE_HEADER) String role) {
        requireRole(role, "DOCTOR");
        return ResponseEntity.ok(
                appointmentService.getTodayAppointmentsForDoctor(parseDoctorId(authenticatedDoctorId))
        );
    }

    @GetMapping("/slots/booked")
    public ResponseEntity<List<String>> getBookedSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // doctorId here is not the caller's identity. A patient is choosing which doctor's
        // availability to view, so it remains a normal business parameter.
        return ResponseEntity.ok(appointmentService.getBookedSlotsForDoctor(doctorId, date));
    }

    private void requireRole(String actualRole, String requiredRole) {
        if (actualRole == null || !requiredRole.equalsIgnoreCase(actualRole)) {
            throw new AuthorizationException("This operation requires " + requiredRole + " access");
        }
    }

    private Long parseDoctorId(String authenticatedDoctorId) {
        try {
            return Long.parseLong(authenticatedDoctorId);
        } catch (NumberFormatException ex) {
            throw new AuthorizationException("Invalid authenticated doctor identity");
        }
    }
}
