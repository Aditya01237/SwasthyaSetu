package com.example.provider_service.controller;

import com.example.provider_service.dto.CommonResponse;
import com.example.provider_service.dto.HospitalDetailsResponse;
import com.example.provider_service.dto.HospitalRegisterRequest;
import com.example.provider_service.dto.HospitalResponse;
import com.example.provider_service.service.HospitalServices;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
public class HospitalController {

    private final HospitalServices hospitalServices;

    public HospitalController(HospitalServices hospitalServices) {
        this.hospitalServices = hospitalServices;
    }

    // ✅ Add Hospital
    @PostMapping("/add")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> registerHospital(
            @RequestBody HospitalRegisterRequest request) {

        HospitalDetailsResponse response = hospitalServices.addHospital(request);

        return ResponseEntity.ok(
                new CommonResponse<>("Hospital Registered Successfully", response, 200)
        );
    }

    // ✅ Get All Hospitals (simple)
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<HospitalDetailsResponse>>> getAllHospital() {

        List<HospitalDetailsResponse> response = hospitalServices.getAllHospitals();

        return ResponseEntity.ok(
                new CommonResponse<>("All Hospital Data", response, 200)
        );
    }

    // ✅ Get Hospital By ID (detailed)
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<HospitalDetailsResponse>> getHospitalById(@PathVariable String id) {

        HospitalDetailsResponse response = hospitalServices.getHospitalById(id);

        return ResponseEntity.ok(
                new CommonResponse<>("Hospital Available", response, 200)
        );
    }

    // ✅ Filter + Search Hospitals (used in dashboard)
    @GetMapping("/list")
    public ResponseEntity<CommonResponse<List<HospitalResponse>>> getHospitals(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name
    ) {

        List<HospitalResponse> hospitals = hospitalServices.getHospitals(city, name);

        return ResponseEntity.ok(
                new CommonResponse<>("Hospitals fetched successfully", hospitals, 200)
        );
    }
}