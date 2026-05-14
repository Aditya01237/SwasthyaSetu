package com.example.provider_service.controller;

import com.example.provider_service.dto.CommonResponse;
import com.example.provider_service.dto.DoctorRegisterRequest;
import com.example.provider_service.dto.DoctorResponse;
import com.example.provider_service.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService){
        this.doctorService = doctorService;
    }

    @PostMapping("/register")
    public ResponseEntity<CommonResponse<DoctorResponse>> registerDoctor(@RequestBody DoctorRegisterRequest registerRequest){
        DoctorResponse response = doctorService.registerDoctor(registerRequest);
        return ResponseEntity.ok(
                new CommonResponse<>("Doctor registered successfully", response, 200)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DoctorResponse>> getDoctorById(@PathVariable Long id){
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(
                new CommonResponse<>("Doctor Find successfully", response, 200)
        );
    }

    @GetMapping("/hospital/{hospitalId}")
    public ResponseEntity<CommonResponse<List<DoctorResponse>>> getDoctors(
            @PathVariable String hospitalId) {

        List<DoctorResponse> doctors = doctorService.getDoctorsByHospital(hospitalId);

        return ResponseEntity.ok(
                new CommonResponse<>("Doctors fetched successfully", doctors, 200)
        );
    }



}
