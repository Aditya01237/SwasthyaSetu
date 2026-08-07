package com.medicine.hospital.controller;

import com.medicine.hospital.dto.CommonResponse;
import com.medicine.hospital.dto.HospitalDetailsResponse;
import com.medicine.hospital.dto.HospitalRegisterRequest;
import com.medicine.hospital.dto.HospitalResponse;
import com.medicine.hospital.security.HospitalAuthorization;
import com.medicine.hospital.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final HospitalService hospitalService;
    private final HospitalAuthorization authorization;

    public HospitalController(HospitalService hospitalService, HospitalAuthorization authorization) {
        this.hospitalService = hospitalService;
        this.authorization = authorization;
    }

    @PostMapping("/add")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> registerHospital(
            @RequestHeader(USER_ROLE_HEADER) String role,
            @RequestBody HospitalRegisterRequest request) {
        authorization.requireAdmin(role);
        HospitalDetailsResponse response = hospitalService.addHospital(request);
        return ResponseEntity.ok(new CommonResponse<>("Hospital Registered Successfully", response, 200));
    }

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<HospitalDetailsResponse>>> getAllHospitals() {
        return ResponseEntity.ok(new CommonResponse<>("All Hospital Data", hospitalService.getAllHospitals(), 200));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> getHospitalById(@PathVariable String id) {
        return ResponseEntity.ok(new CommonResponse<>("Hospital Available", hospitalService.getHospitalById(id), 200));
    }

    @GetMapping("/list")
    public ResponseEntity<CommonResponse<List<HospitalResponse>>> getHospitals(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(
                new CommonResponse<>("Hospitals fetched successfully", hospitalService.getHospitals(city, name), 200)
        );
    }
}
