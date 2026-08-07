package com.medicine.hospital.controller;

import com.medicine.hospital.dto.CommonResponse;
import com.medicine.hospital.dto.DoctorRegisterRequest;
import com.medicine.hospital.dto.DoctorResponse;
import com.medicine.hospital.security.HospitalAuthorization;
import com.medicine.hospital.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String HOSPITAL_ID_HEADER = "X-Hospital-Id";

    private final DoctorService doctorService;
    private final HospitalAuthorization authorization;

    public DoctorController(DoctorService doctorService, HospitalAuthorization authorization) {
        this.doctorService = doctorService;
        this.authorization = authorization;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<DoctorResponse>> registerDoctor(
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestHeader(value = HOSPITAL_ID_HEADER, required = false) String authenticatedHospitalId,
            @RequestBody DoctorRegisterRequest request) {
        authorization.requireHospitalWriteAccess(role, authenticatedHospitalId, request.getHospitalId());
        DoctorResponse response = doctorService.registerDoctor(request);
        return ResponseEntity.ok(new CommonResponse<>("Doctor registered successfully", response, 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(new CommonResponse<>("Doctor Find successfully", response, 200));
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<CommonResponse<List<DoctorResponse>>> getDoctors(@PathVariable String hospitalId) {
        List<DoctorResponse> doctors = doctorService.getDoctorsByHospital(hospitalId);
        return ResponseEntity.ok(new CommonResponse<>("Doctors fetched successfully", doctors, 200));
    }
}
