package com.medicine.hospital.controller;

import com.medicine.hospital.dto.CommonResponse;
import com.medicine.hospital.dto.DoctorRegisterRequest;
import com.medicine.hospital.dto.DoctorResponse;
import com.medicine.hospital.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<DoctorResponse>> registerDoctor(@RequestBody DoctorRegisterRequest request) {
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
